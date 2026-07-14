package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import java.util.List;

/**
 * Service cho thao tác với đăng ký thi (ExamRegistration).
 * Cung cấp các phương thức cập nhật điểm danh, thanh toán, phân phòng,
 * điểm số và xử lý nghiệp vụ đăng ký tham gia kỳ thi sát hạch lái xe.
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
     * Lấy thông tin đăng ký thi theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd       số báo danh
     * @return ExamRegistrationDTO, hoặc null nếu không tìm thấy
     */
    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    /**
     * Lấy danh sách thí sinh đã đăng ký theo mã kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách ExamRegistrationDTO
     */
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

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
     * Cập nhật phòng thi đã phân cho đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return true nếu cập nhật thành công
     */
    boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Cập nhật sân/phòng thực hành đã phân cho đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực thực hành
     * @param areaName    tên khu vực thực hành
     * @return true nếu cập nhật thành công
     */
    boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

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

    /**
     * Xóa / hủy đánh dấu các khoản thanh toán đã hoàn thành của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @return true nếu cập nhật thành công
     */
    boolean clearCompletedPayments(int candidateId);

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
}
