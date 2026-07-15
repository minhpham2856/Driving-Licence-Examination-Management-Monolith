package examstaff.service.impl;

import examstaff.dao.ExamRegistrationDAO;
import examstaff.dao.impl.ExamRegistrationDAOImpl;
import examstaff.service.ExamRegistrationService;
import examstaff.dto.ExamRegistrationDTO;
import java.util.List;

/** Implementation: uỷ quyền CRUD đăng ký thí sinh xuống {@link ExamRegistrationDAO}. */
public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final ExamRegistrationDAO dao = new ExamRegistrationDAOImpl();

    /**
     * Lấy thông tin đăng ký thi theo mã, trả về DTO.
     *
     * @param id mã đăng ký thi
     * @return ExamRegistrationDTO, hoặc null nếu không tìm thấy
     */
    @Override
    public ExamRegistrationDTO getById(int id) {
        return dao.getById(id);
    }

    /**
     * Lấy thông tin đăng ký thi theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd       số báo danh
     * @return ExamRegistrationDTO, hoặc null nếu không tìm thấy
     */
    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        return dao.getByExamAndSbd(examId, sbd);
    }

    /**
     * Lấy danh sách thí sinh đã đăng ký theo mã kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách ExamRegistrationDTO
     */
    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        return dao.getCandidatesByExam(examId);
    }

    /**
     * Cập nhật trạng thái điểm danh cho đăng ký thi.
     *
     * @param id        mã đăng ký thi
     * @param isPresent true nếu có mặt
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        return dao.updatePresent(id, isPresent);
    }

    /**
     * Cập nhật trạng thái thanh toán cho đăng ký thi.
     *
     * @param id                mã đăng ký thi
     * @param isPaymentCompleted true nếu đã thanh toán
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        return dao.updatePayment(id, isPaymentCompleted);
    }

    /**
     * Cập nhật phòng thi đã phân cho đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updateAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    /**
     * Cập nhật sân/phòng thực hành đã phân cho đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực thực hành
     * @param areaName    tên khu vực thực hành
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updatePracticalAllocatedRoom(candidateId, examId, areaId, areaName);
    }

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
    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

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
    @Override
    public boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        return dao.updateProfile(id, fullName, dob, govIdNo, email, phoneNo);
    }

    /**
     * Cập nhật ảnh cho đăng ký thi.
     *
     * @param id       mã đăng ký thi
     * @param photoUrl đường dẫn ảnh
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        return dao.updatePhoto(id, photoUrl);
    }

    /**
     * Xóa / hủy đánh dấu các khoản thanh toán đã hoàn thành của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean clearCompletedPayments(int candidateId) {
        return dao.clearCompletedPayments(candidateId);
    }

    /**
     * Đánh dấu thí sinh vắng mặt trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu đánh dấu thành công
     */
    @Override
    public boolean markAbsent(int candidateId) {
        return dao.markAbsent(candidateId);
    }

    /**
     * Hủy đánh dấu vắng mặt cho thí sinh trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu hủy thành công
     */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return dao.clearAbsentMarking(candidateId);
    }

    /**
     * Đánh dấu thí sinh bị đình chỉ thi trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu đánh dấu thành công
     */
    @Override
    public boolean markSuspended(int candidateId) {
        return dao.markSuspended(candidateId);
    }

    /**
     * Hủy đình chỉ thi cho thí sinh trong đăng ký thi.
     *
     * @param candidateId mã thí sinh
     * @return true nếu hủy thành công
     */
    @Override
    public boolean undoSuspension(int candidateId) {
        return dao.undoSuspension(candidateId);
    }
}
