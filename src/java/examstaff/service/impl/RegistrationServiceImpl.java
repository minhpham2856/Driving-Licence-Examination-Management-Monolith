package examstaff.service.impl;

import examstaff.dao.ExamRegistrationDAO;
import examstaff.dao.impl.ExamRegistrationDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.service.RegistrationService;

import java.sql.Date;
import java.util.List;

/**
 * Implementation RegistrationService: CRUD đăng ký thí sinh qua ExamRegistrationDAO.
 *
 * Phân tách với hàng đợi UI:
 * - <b>Entity đầy đủ</b> — getCandidatesByExam cho workflow phân bổ / thủ tục
 *       cần reload toàn bộ trường từ DB
 * - <b>UI hàng đợi staff</b> — dùng StaffCallService.listQueueByExamId
 *       (View DAO); <b>không</b> dùng getCandidatesByExam trên màn gọi số
 * Mọi method mutate (updatePresent, updatePayment, phòng, điểm, ảnh,
 * vắng mặt, đình chỉ) ủy quyền trực tiếp sang ExamRegistrationDAO.
 */
public class RegistrationServiceImpl implements RegistrationService {

    private final ExamRegistrationDAO dao = new ExamRegistrationDAOImpl();

    /**
     * Ủy quyền sang ExamRegistrationDAO.getById.
     * @param id mã thí sinh
     * @return DTO hoặc null
     */
    @Override
    public ExamRegistrationDTO getById(int id) {
        return dao.getById(id);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.getByExamAndSbd.
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return DTO hoặc null
     */
    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        return dao.getByExamAndSbd(examId, sbd);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.getCandidatesByExam.
     * @param examId mã kỳ thi
     * @return danh sách đăng ký
     */
    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        return dao.getCandidatesByExam(examId);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updatePresent.
     * @param id        mã thí sinh
     * @param isPresent có mặt hay không
     * @return true nếu thành công
     */
    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        return dao.updatePresent(id, isPresent);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updatePayment.
     * @param id                 mã thí sinh
     * @param isPaymentCompleted đã thanh toán hay không
     * @return true nếu thành công
     */
    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        return dao.updatePayment(id, isPaymentCompleted);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updateAllocatedRoom.
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return true nếu thành công
     */
    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updateAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updatePracticalAllocatedRoom.
     * @param candidateId mã thí sinh
     * @param examId      mã kỳ thi
     * @param areaId      mã khu vực
     * @param areaName    tên khu vực
     * @return true nếu thành công
     */
    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updatePracticalAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updateScores.
     * @param id              mã thí sinh
     * @param theoryScore     điểm LT (null = bỏ qua)
     * @param theoryPassed    cờ kết quả LT
     * @param practicalScore  điểm TH (null = bỏ qua)
     * @param practicalPassed cờ kết quả TH
     * @return true nếu thành công
     */
    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updateProfile.
     * @param id       mã thí sinh
     * @param fullName họ tên
     * @param dob      ngày sinh
     * @param govIdNo  CCCD/CMND
     * @param email    email
     * @param phoneNo  số điện thoại
     * @return true nếu thành công
     */
    @Override
    public boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
        return dao.updateProfile(id, fullName, dob, govIdNo, email, phoneNo);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.updatePhoto.
     * @param id       mã thí sinh
     * @param photoUrl đường dẫn ảnh
     * @return true nếu thành công
     */
    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        return dao.updatePhoto(id, photoUrl);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.clearCompletedPayments.
     * @param candidateId mã thí sinh
     * @return true nếu thành công
     */
    @Override
    public boolean clearCompletedPayments(int candidateId) {
        return dao.clearCompletedPayments(candidateId);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.markAbsent.
     * @param candidateId mã thí sinh
     * @return true nếu thành công
     */
    @Override
    public boolean markAbsent(int candidateId) {
        return dao.markAbsent(candidateId);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.clearAbsentMarking.
     * @param candidateId mã thí sinh
     * @return true nếu thành công
     */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return dao.clearAbsentMarking(candidateId);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.markSuspended.
     * @param candidateId mã thí sinh
     * @return true nếu thành công
     */
    @Override
    public boolean markSuspended(int candidateId) {
        return dao.markSuspended(candidateId);
    }

    /**
     * Ủy quyền sang ExamRegistrationDAO.undoSuspension.
     * @param candidateId mã thí sinh
     * @return true nếu thành công
     */
    @Override
    public boolean undoSuspension(int candidateId) {
        return dao.undoSuspension(candidateId);
    }
}
