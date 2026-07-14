package examstaff.service.impl;

import examstaff.dao.ExamRegistrationDAO;
import examstaff.dao.impl.ExamRegistrationDAOImpl;
import examstaff.service.ExamRegistrationService;
import examstaff.dto.exam.ExamRegistrationDTO;
import java.util.List;

/** Implementation: uỷ quyền CRUD đăng ký thí sinh xuống {@link ExamRegistrationDAO}. */
public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final ExamRegistrationDAO dao = new ExamRegistrationDAOImpl();

    /** {@inheritDoc} */
    @Override
    public ExamRegistrationDTO getById(int id) {
        return dao.getById(id);
    }

    /** {@inheritDoc} */
    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        return dao.getByExamAndSbd(examId, sbd);
    }

    /** {@inheritDoc} */
    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        return dao.getCandidatesByExam(examId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        return dao.updatePresent(id, isPresent);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        return dao.updatePayment(id, isPaymentCompleted);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updateAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updatePracticalAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        return dao.updateProfile(id, fullName, dob, govIdNo, email, phoneNo);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        return dao.updatePhoto(id, photoUrl);
    }

    /** {@inheritDoc} */
    @Override
    public boolean clearCompletedPayments(int candidateId) {
        return dao.clearCompletedPayments(candidateId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean markAbsent(int candidateId) {
        return dao.markAbsent(candidateId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return dao.clearAbsentMarking(candidateId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean markSuspended(int candidateId) {
        return dao.markSuspended(candidateId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean undoSuspension(int candidateId) {
        return dao.undoSuspension(candidateId);
    }
}
