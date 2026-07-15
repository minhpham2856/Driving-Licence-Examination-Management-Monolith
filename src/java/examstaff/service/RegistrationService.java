package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;

import java.sql.Date;
import java.util.List;

/** Alias/wrap đăng ký thí sinh kỳ thi. */
public interface RegistrationService {

    /**
     * Lấy đăng ký theo mã thí sinh.
     *
     * @param id mã thí sinh
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
     * Danh sách thí sinh theo kỳ thi (entity đầy đủ cho workflow).
     *
     * @param examId mã kỳ thi
     * @return danh sách đăng ký
     */
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    /**
     * Cập nhật cờ có mặt.
     *
     * @param id        mã thí sinh
     * @param isPresent có mặt hay không
     * @return {@code true} nếu thành công
     */
    boolean updatePresent(int id, boolean isPresent);

    /**
     * Cập nhật / ghi nhận thanh toán hoàn tất.
     *
     * @param id                 mã thí sinh
     * @param isPaymentCompleted đã thanh toán hay không
     * @return {@code true} nếu thành công
     */
    boolean updatePayment(int id, boolean isPaymentCompleted);

    /**
     * Cập nhật phòng phân bổ lý thuyết.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return {@code true} nếu thành công
     */
    boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Cập nhật sân/phòng phân bổ thực hành.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return {@code true} nếu thành công
     */
    boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Cập nhật điểm lý thuyết và/hoặc thực hành.
     *
     * @param id              mã thí sinh
     * @param theoryScore     điểm LT ({@code null} = bỏ qua)
     * @param theoryPassed    cờ kết quả LT
     * @param practicalScore  điểm TH ({@code null} = bỏ qua)
     * @param practicalPassed cờ kết quả TH
     * @return {@code true} nếu thành công
     */
    boolean updateScores(int id, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    /**
     * Cập nhật hồ sơ cơ bản thí sinh.
     *
     * @param id       mã thí sinh
     * @param fullName họ tên
     * @param dob      ngày sinh
     * @param govIdNo  CCCD/CMND
     * @param email    email
     * @param phoneNo  số điện thoại
     * @return {@code true} nếu thành công
     */
    boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo);

    /**
     * Cập nhật đường dẫn ảnh thí sinh.
     *
     * @param id       mã thí sinh
     * @param photoUrl URL/đường dẫn ảnh
     * @return {@code true} nếu thành công
     */
    boolean updatePhoto(int id, String photoUrl);

    /**
     * Xóa các giao dịch thanh toán đã hoàn tất.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thành công
     */
    boolean clearCompletedPayments(int candidateId);

    /**
     * Đánh dấu vắng mặt.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thành công
     */
    boolean markAbsent(int candidateId);

    /**
     * Hủy đánh dấu vắng mặt.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thành công
     */
    boolean clearAbsentMarking(int candidateId);

    /**
     * Đánh dấu đình chỉ thi.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thành công
     */
    boolean markSuspended(int candidateId);

    /**
     * Hủy đình chỉ thi.
     *
     * @param candidateId mã thí sinh
     * @return {@code true} nếu thành công
     */
    boolean undoSuspension(int candidateId);
}
