package examstaff.dao;

import examstaff.dto.ExamRegistrationDTO;
import java.util.List;

/**
 * Cổng truy cập hồ sơ / ghi danh thí sinh trong kỳ thi — <b>read + write</b> trên nhiều bảng.
 *
 * Vai trò trong kiến trúc:
 * DAO trung tâm cho dashboard thí sinh, thủ tục (procedure), gọi số, phân bổ phòng.
 * Đọc qua {@link Db2CandidateSql#CANDIDATE_SELECT}; ghi rải trên {@code Candidate},
 * {@code ExamEnrollment}, {@code ExamEnrollmentSection}, {@code Payment}, {@code ExamScore}…
 * <pre>
 *   ProcedureServlet / AllocationServlet / CandidateCallServlet
 *            │
 *            ▼
 *      ExamRegistrationDAO  ◄── ExamRegistrationDAOImpl
 *            │                      │
 *            │                      ├── ExamEnrollmentSectionSupport (LT/TH allocation)
 *            │                      └── AllocationPassRules (điểm đạt/rớt)
 *            ▼
 *      ExamRegistrationDTO → UI / service
 * </pre>
 *
 * Nhóm thao tác chính:
 * - <b>Đọc</b> — {@link #getById}, {@link #getByExamAndSbd}, {@link #getCandidatesByExam}
 * - <b>Thủ tục</b> — có mặt/vắng, ảnh, thanh toán, hồ sơ ({@link #updatePresent}, {@link #updatePayment}…)
 * - <b>Phân phòng</b> — LT/TH ({@link #updateAllocatedRoom}, {@link #updatePracticalAllocatedRoom})
 * - <b>Điểm & kỷ luật</b> — {@link #updateScores}, đình chỉ, vắng ({@link #markAbsent}…)
 *
 * Khác ExamStaffCandidateViewDAO:
 * View DAO chỉ đọc {@link examstaff.dto.ExamStaffCandidate} cho list nhẹ; interface này map đầy đủ
 * {@link ExamRegistrationDTO} và thực hiện mọi UPDATE/INSERT nghiệp vụ.
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ExamRegistrationDAOImpl}.
 */
public interface ExamRegistrationDAO {

    /**
     * Lấy đăng ký theo mã thí sinh.
     * Thực thi SELECT qua {@link Db2CandidateSql#CANDIDATE_SELECT}
     * với {@code WHERE c.CandidateId = ?} (JOIN Enrollment, Licence, Section, Payment, điểm…).
     * @param id mã thí sinh ({@code CandidateId}) cần tìm
     * @return {@link ExamRegistrationDTO} nếu tìm thấy; {@code null} nếu không có
     */
    ExamRegistrationDTO getById(int id);

    /**
     * Lấy thí sinh theo kỳ thi và số báo danh (SBD).
     * Thực thi SELECT {@link Db2CandidateSql#CANDIDATE_SELECT} lọc {@code ExamId}
     * và số báo danh parse từ {@code CandidateNumber}; fallback quét danh sách kỳ thi nếu SBD chữ.
     * @param examId mã kỳ thi ({@code ExamEnrollment.ExamId})
     * @param sbd    số báo danh thí sinh (chuỗi SBD hoặc phần số)
     * @return {@link ExamRegistrationDTO} nếu tìm thấy; {@code null} nếu không khớp / SBD trống
     */
    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    /**
     * Danh sách thí sinh theo kỳ thi.
     * Thực thi SELECT {@link Db2CandidateSql#CANDIDATE_SELECT} (hoặc bản MINIMAL fallback)
     * với {@code WHERE ee.ExamId = ?}.
     * @param examId mã kỳ thi cần liệt kê thí sinh
     * @return danh sách {@link ExamRegistrationDTO}; rỗng nếu kỳ thi không có ghi danh
     */
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    /**
     * Cập nhật cờ có mặt của thí sinh.
     * Khi đánh dấu có mặt: UPDATE {@code Candidate.IsAbsent = 0} (và luồng liên quan);
     * khi vắng: ủy quyền {@link #markAbsent(int)}.
     * @param id        mã thí sinh ({@code CandidateId})
     * @param isPresent {@code true} = có mặt; {@code false} = vắng mặt
     * @return {@code true} nếu thao tác thành công; {@code false} nếu thất bại
     */
    boolean updatePresent(int id, boolean isPresent);

    /**
     * Cập nhật / tạo thanh toán hoàn tất cho thí sinh.
     * Khi hoàn tất: tìm hoặc INSERT {@code Payment} gắn {@code ExamEnrollment};
     * khi hủy: DELETE các payment đã hoàn tất liên quan thí sinh.
     * @param id                 mã thí sinh ({@code CandidateId})
     * @param isPaymentCompleted {@code true} = đã thanh toán; {@code false} = xóa payment hoàn tất
     * @return {@code true} nếu thành công; {@code false} nếu thất bại
     */
    boolean updatePayment(int id, boolean isPaymentCompleted);

    /**
     * Cập nhật phòng phân bổ lý thuyết cho thí sinh trong kỳ thi.
     * UPDATE {@code ExamEnrollmentSection.ExamAreaId} (và có thể {@code ExamEnrollment.AllocatedExamAreaId})
     * cho section lý thuyết; đảm bảo section tồn tại trước khi gán.
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực / phòng ({@code ExamAreaId})
     * @param areaName    tên khu vực (có thể chỉ dùng hiển thị, không bắt buộc persistence)
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Cập nhật sân / phòng phân bổ thực hành cho thí sinh trong kỳ thi.
     * UPDATE {@code ExamEnrollmentSection.ExamAreaId} cho section thực hành
     * (sa hình / practical) theo {@code ExamId} và thí sinh.
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực thực hành ({@code ExamAreaId})
     * @param areaName    tên khu vực (có thể chỉ dùng hiển thị)
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Kiểm tra thí sinh đã có phòng lý thuyết trong kỳ thi chưa (ràng buộc phân bổ duy nhất).
     * SELECT tên / mã phòng từ {@code ExamEnrollmentSection} JOIN {@code ExamSection}/{@code ExamArea}
     * phần lý thuyết; trả thông báo lỗi nếu đã phân phòng.
     * @param candidateId mã thí sinh cần kiểm tra
     * @param examId      mã kỳ thi
     * @return thông báo lỗi tiếng Việt nếu đã có phòng; {@code null} nếu được phép phân phòng
     */
    String validateUniqueTheoryAllocation(int candidateId, int examId);

    /**
     * Cập nhật điểm lý thuyết và/hoặc thực hành của thí sinh.
     * Ghi {@code ExamResult} / {@code ExamScore} theo section LT–TH; bỏ qua cột nào có tham số {@code null}.
     * @param id              mã thí sinh ({@code CandidateId})
     * @param theoryScore     điểm lý thuyết; {@code null} = không cập nhật LT
     * @param theoryPassed    kết quả LT ({@code passed}/{@code failed}); {@code null} = bỏ qua trạng thái
     * @param practicalScore  điểm thực hành; {@code null} = không cập nhật TH
     * @param practicalPassed kết quả TH ({@code passed}/{@code failed}); {@code null} = bỏ qua trạng thái
     * @return {@code true} nếu ghi điểm thành công; {@code false} nếu thất bại
     */
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    /**
     * Cập nhật hồ sơ cơ bản thí sinh và dữ liệu Profile / User liên quan.
     * UPDATE {@code Candidate}; đồng bộ {@code Profile} theo CCCD và {@code [User].Email} nếu có liên kết.
     * @param id       mã thí sinh ({@code CandidateId})
     * @param fullName họ tên mới
     * @param dob      ngày sinh mới
     * @param govIdNo  số CCCD / CMND mới
     * @param email    email mới
     * @param phoneNo  số điện thoại mới
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    /**
     * Cập nhật đường dẫn ảnh thí sinh.
     * Thực thi {@code UPDATE Candidate SET PhotoImageUrl = ? WHERE CandidateId = ?}.
     * @param id       mã thí sinh
     * @param photoUrl URL / đường dẫn ảnh mới
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean updatePhoto(int id, String photoUrl);

    /**
     * Xóa các giao dịch thanh toán đã hoàn tất của thí sinh.
     * DELETE trên {@code Payment} JOIN {@code ExamEnrollment} theo {@code CandidateId}
     * với trạng thái thanh toán hoàn tất.
     * @param candidateId mã thí sinh cần xóa payment hoàn tất
     * @return {@code true} nếu thao tác thành công; {@code false} nếu thất bại
     */
    boolean clearCompletedPayments(int candidateId);

    /**
     * Đánh dấu thí sinh vắng mặt.
     * Thực thi {@code UPDATE Candidate SET IsAbsent = 1 WHERE CandidateId = ?}
     * (và có thể dọn kết quả / section liên quan tùy triển khai).
     * @param candidateId mã thí sinh cần đánh dấu vắng
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean markAbsent(int candidateId);

    /**
     * Hủy đánh dấu vắng mặt và dọn kết quả liên quan nếu cần.
     * UPDATE {@code Candidate.IsAbsent = 0}; có thể DELETE {@code DeductionRecord},
     * {@code ExamScore}, {@code ExamResult} của enrollment tương ứng.
     * @param candidateId mã thí sinh cần hủy vắng
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean clearAbsentMarking(int candidateId);

    /**
     * Đánh dấu đình chỉ thi cho thí sinh.
     * Thực thi {@code UPDATE Candidate SET IsSuspended = 1 WHERE CandidateId = ?}.
     * @param candidateId mã thí sinh cần đình chỉ
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean markSuspended(int candidateId);

    /**
     * Hủy đình chỉ thi cho thí sinh.
     * Thực thi {@code UPDATE Candidate SET IsSuspended = 0 WHERE CandidateId = ?}.
     * @param candidateId mã thí sinh cần hủy đình chỉ
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean undoSuspension(int candidateId);
}
