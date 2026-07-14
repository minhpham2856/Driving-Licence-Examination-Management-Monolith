package examstaff.dao;

import examstaff.dto.exam.ExamRegistrationDTO;
import java.util.List;

/**
 * DAO đăng ký / hồ sơ thí sinh trong kỳ thi (Candidate + ExamEnrollment).
 */
public interface ExamRegistrationDAO {

    /**
     * Lấy đăng ký theo mã thí sinh.
     *
     * @param id mã thí sinh ({@code CandidateId})
     * @return DTO hoặc {@code null}
     */
    ExamRegistrationDTO getById(int id);

    /**
     * Lấy thí sinh theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return DTO hoặc {@code null}
     */
    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    /**
     * Danh sách thí sinh theo kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách đăng ký
     */
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    /**
     * Cập nhật cờ có mặt (xóa đánh dấu vắng nếu cần).
     *
     * @param id        mã thí sinh
     * @param isPresent có mặt hay không
     * @return {@code true} nếu thao tác thành công
     */
    boolean updatePresent(int id, boolean isPresent);

    /**
     * Cập nhật / tạo thanh toán hoàn tất cho thí sinh.
     *
     * @param id                  mã thí sinh
     * @param isPaymentCompleted  đã thanh toán hay không
     * @return {@code true} nếu thành công
     */
    boolean updatePayment(int id, boolean isPaymentCompleted);

    /**
     * Cập nhật phòng phân bổ lý thuyết.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực (có thể không dùng ở persistence)
     * @return {@code true} nếu cập nhật thành công
     */
    boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Cập nhật sân/phòng phân bổ thực hành.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return {@code true} nếu cập nhật thành công
     */
    boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Kiểm tra thí sinh đã có phòng lý thuyết trong kỳ thi chưa.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @return thông báo lỗi, hoặc {@code null} nếu được phép phân phòng
     */
    String validateUniqueTheoryAllocation(int candidateId, int examId);

    /**
     * Cập nhật điểm lý thuyết và/hoặc thực hành.
     *
     * @param id              mã thí sinh
     * @param theoryScore     điểm lý thuyết (null = bỏ qua)
     * @param theoryPassed    kết quả LT (passed/failed/null)
     * @param practicalScore  điểm thực hành (null = bỏ qua)
     * @param practicalPassed kết quả TH (passed/failed/null)
     * @return {@code true} nếu ghi điểm thành công
     */
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    /**
     * Cập nhật hồ sơ cơ bản thí sinh (và Profile/User liên quan).
     *
     * @param id       mã thí sinh
     * @param fullName họ tên
     * @param dob      ngày sinh
     * @param govIdNo  CCCD/CMND
     * @param email    email
     * @param phoneNo  số điện thoại
     * @return {@code true} nếu cập nhật thành công
     */
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    /**
     * Cập nhật đường dẫn ảnh thí sinh.
     *
     * @param id       mã thí sinh
     * @param photoUrl URL ảnh
     * @return {@code true} nếu cập nhật thành công
     */
    boolean updatePhoto(int id, String photoUrl);

    /**
     * Xóa các giao dịch thanh toán đã hoàn tất của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thao tác thành công
     */
    boolean clearCompletedPayments(int candidateId);

    /**
     * Đánh dấu vắng mặt.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu cập nhật thành công
     */
    boolean markAbsent(int candidateId);

    /**
     * Hủy đánh dấu vắng (và dọn kết quả liên quan).
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu cập nhật thành công
     */
    boolean clearAbsentMarking(int candidateId);

    /**
     * Đánh dấu đình chỉ thi.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu cập nhật thành công
     */
    boolean markSuspended(int candidateId);

    /**
     * Hủy đình chỉ thi.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu cập nhật thành công
     */
    boolean undoSuspension(int candidateId);
}
