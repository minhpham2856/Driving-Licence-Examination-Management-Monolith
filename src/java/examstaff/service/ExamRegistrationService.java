package examstaff.service;


import examstaff.dto.exam.ExamRegistrationDTO;

import shared.model.ExamRegistration;
import java.util.List;

/**
 * DAO cho thao tác với đăng ký thi (ExamRegistration).
 * Cung cấp các phương thức CRUD, cập nhật điểm danh, thanh toán, thiết bị,
 * điểm số và xử lý nghiệp vụ đăng ký tham gia kỳ thi sát hạch lái xe.
 * getById() trả về DTO; findById() trả về Model.
 */
public interface ExamRegistrationService {

    /**
     * Lấy thông tin đăng ký thi theo mã, trả về DTO.
     *
     * @param id mã đăng ký thi
     * @return ExamRegistrationDTO, hoặc null nếu không tìm thấy
     */
    ExamRegistrationDTO getById(int id);

    /**
     * Lấy thông tin đăng ký thi theo mã, trả về Model.
     *
     * @param id mã đăng ký thi
     * @return ExamRegistration model, hoặc null nếu không tìm thấy
     */
    ExamRegistration findById(int id);

    /**
     * Lấy thông tin đăng ký thi theo kỳ thi và số báo danh.
     *
     * @param sessionId mã kỳ thi
     * @param sbd       số báo danh
     * @return ExamRegistrationDTO, hoặc null nếu không tìm thấy
     */
    ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd);

    /**
     * Lấy danh sách thí sinh đã đăng ký theo mã kỳ thi.
     *
     * @param sessionId mã kỳ thi
     * @return danh sách ExamRegistrationDTO
     */
    List<ExamRegistrationDTO> getCandidatesBySession(int sessionId);

    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    /**
     * Cập nhật trạng thái điểm danh cho đăng ký thi.
     *
     * @param id        mã đăng ký thi
     * @param isPresent true nếu có mặt
     * @return true nếu cập nhật thành công
     */
    boolean updatePresent(int id, boolean isPresent);

    /**
     * Cập nhật trạng thái thanh toán cho đăng ký thi.
     *
     * @param id                mã đăng ký thi
     * @param isPaymentCompleted true nếu đã thanh toán
     * @return true nếu cập nhật thành công
     */
    boolean updatePayment(int id, boolean isPaymentCompleted);

    /**
     * Cập nhật mã máy tính cho đăng ký thi.
     *
     * @param id           mã đăng ký thi
     * @param computerCode mã máy tính
     * @return true nếu cập nhật thành công
     */
    boolean updateComputer(int id, String computerCode);

    /**
     * Cập nhật phòng thi đã phân cho đăng ký thi.
     *
     * @param id       mã đăng ký thi
     * @param areaId   mã khu vực
     * @param areaName tên khu vực
     * @return true nếu cập nhật thành công
     */
    boolean updateAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName);

    boolean updatePracticalAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName);

    /**
     * @return thông báo lỗi nếu thí sinh đã có phòng ở ca khác trong cùng kỳ thi; null nếu hợp lệ
     */
    String validateUniqueTheoryAllocation(int candidateId, int sessionId);

    /**
     * Cập nhật thiết bị cho đăng ký thi.
     *
     * @param id         mã đăng ký thi
     * @param deviceCode mã thiết bị
     * @return true nếu cập nhật thành công
     */
    boolean updateDevice(int id, String deviceCode);

    /**
     * Cập nhật điểm số cho đăng ký thi.
     *
     * @param id               mã đăng ký thi
     * @param theoryScore      điểm lý thuyết (có thể null)
     * @param theoryPassed     kết quả đỗ/trượt lý thuyết
     * @param practicalScore   điểm thực hành (có thể null)
     * @param practicalPassed  kết quả đỗ/trượt thực hành
     * @return true nếu cập nhật thành công
     */
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    /**
     * Cập nhật điểm lý thuyết dưới dạng số câu đúng (0–35) với ngưỡng đạt.
     *
     * @param id            mã đăng ký thi
     * @param correctCount  số câu trả lời đúng
     * @param passThreshold ngưỡng số câu đúng tối thiểu để đạt
     * @return true nếu cập nhật thành công
     */
    boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold);

    /**
     * Cập nhật thông tin hồ sơ cơ bản của đăng ký thi.
     *
     * @param id       mã đăng ký thi
     * @param fullName họ và tên
     * @param dob      ngày sinh
     * @param govIdNo  số CMND/CCCD
     * @param email    địa chỉ email
     * @param phoneNo  số điện thoại
     * @return true nếu cập nhật thành công
     */
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);


    /**
     * Cập nhật ảnh cho đăng ký thi.
     *
     * @param id       mã đăng ký thi
     * @param photoUrl đường dẫn ảnh
     * @return true nếu cập nhật thành công
     */
    boolean updatePhoto(int id, String photoUrl);

    boolean clearCompletedPayments(int candidateId);

    /**
     * Thêm mới một đăng ký thi.
     *
     * @param reg đối tượng ExamRegistrationDTO chứa thông tin đăng ký
     * @return true nếu thêm thành công
     */
    boolean insert(ExamRegistrationDTO reg);

    /**
     * Lấy danh sách tất cả đăng ký thi.
     *
     * @return danh sách tất cả ExamRegistrationDTO
     */
    List<ExamRegistrationDTO> getAllCandidates();

    /**
     * Đánh dấu thí sinh vắng mặt trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu đánh dấu thành công
     */
    boolean markAbsent(int candidateId);

    /**
     * Hủy đánh dấu vắng mặt cho thí sinh trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu hủy thành công
     */
    boolean clearAbsentMarking(int candidateId);

    /**
     * Tìm mã đăng ký thi theo mã hồ sơ và mã kỳ thi.
     *
     * @param profileId mã hồ sơ
     * @param sessionId mã kỳ thi
     * @return Integer mã đăng ký thi, hoặc null nếu không tìm thấy
     */
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    /**
     * Tìm CandidateId theo CCCD và ca thi.
     *
     * @param govId     số CCCD
     * @param sessionId mã ca thi
     * @return CandidateId hoặc null
     */
    Integer findCandidateIdByGovIdAndSession(String govId, int sessionId);

    /**
     * Áp dụng các khoản trừ điểm cho một phần thi và tính lại ExamScore.
     *
     * @param candidateId    mã thí sinh
     * @param deductionIds   mảng mã các khoản trừ điểm
     * @param sectionKeyword từ khóa xác định phần thi (theory/practical)
     * @return true nếu áp dụng thành công
     */
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    /**
     * Điều chỉnh số lần xuất hiện (+1 / -1) của một khoản trừ điểm trong chấm thực hành.
     *
     * @param candidateId mã thí sinh
     * @param sessionId   mã kỳ thi
     * @param deductionId mã khoản trừ điểm
     * @param delta       giá trị điều chỉnh (+1 hoặc -1)
     * @return true nếu điều chỉnh thành công
     */
    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta);

    /**
     * Tính lại điểm thực hành từ các khoản trừ và đánh dấu phần thi đang chờ ký.
     *
     * @param candidateId    mã thí sinh
     * @param sessionId      mã kỳ thi
     * @param sectionKeyword từ khóa xác định phần thi
     * @return true nếu hoàn tất thành công
     */
    boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword);

    /**
     * Lấy danh sách các khoản trừ điểm đã áp dụng cho thí sinh trong kỳ thi.
     *
     * @param candidateId mã thí sinh
     * @param sessionId   mã kỳ thi
     * @return danh sách Map chứa thông tin các khoản trừ điểm
     */
    java.util.List<java.util.Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    /**
     * Đánh dấu thí sinh bị đình chỉ thi trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu đánh dấu thành công
     */
    boolean markSuspended(int candidateId);

    /**
     * Hủy đình chỉ thi cho thí sinh trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu hủy thành công
     */
    boolean undoSuspension(int candidateId);

    /**
     * Đồng bộ trạng thái phần thi cho tất cả thí sinh trong kỳ thi.
     *
     * @param sessionId mã kỳ thi
     */
    void syncSectionStatusesForSession(int sessionId);

    /**
     * Đánh dấu đã in chữ ký cho thí sinh.
     *
     * @param candidateId mã thí sinh
     * @param sessionId   mã kỳ thi
     * @return true nếu đánh dấu thành công
     */
    boolean markSignaturePrinted(int candidateId, int sessionId);

    /**
     * Hoàn tất phần thi cho thí sinh.
     *
     * @param candidateId mã thí sinh
     * @param sessionId   mã kỳ thi
     * @return true nếu hoàn tất thành công
     */
    boolean completeSection(int candidateId, int sessionId);
}



