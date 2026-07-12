package examstaff.service.impl;

import examstaff.dao.ExamRegistrationDAO;
import examstaff.dao.impl.ExamRegistrationDAOImpl;
import examstaff.service.ExamRegistrationService;
import examstaff.dto.exam.ExamRegistrationDTO;
import java.util.List;

public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final ExamRegistrationDAO dao = new ExamRegistrationDAOImpl();

    @Override
    public ExamRegistrationDTO getById(int id) {
        return dao.getById(id);
    }

    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        return dao.getByExamAndSbd(examId, sbd);
    }

    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        return dao.getCandidatesByExam(examId);
    }

    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        return dao.updatePresent(id, isPresent);
    }

    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        return dao.updatePayment(id, isPaymentCompleted);
    }

    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updateAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        return dao.updatePracticalAllocatedRoom(candidateId, examId, areaId, areaName);
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    @Override
    public boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        return dao.updateProfile(id, fullName, dob, govIdNo, email, phoneNo);
    }

    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        return dao.updatePhoto(id, photoUrl);
    }

    @Override
    public boolean clearCompletedPayments(int candidateId) {
        return dao.clearCompletedPayments(candidateId);
    }

    @Override
    public boolean markAbsent(int candidateId) {
        return dao.markAbsent(candidateId);
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return dao.clearAbsentMarking(candidateId);
    }

    @Override
    public boolean markSuspended(int candidateId) {
        return dao.markSuspended(candidateId);
    }

    @Override
    public boolean undoSuspension(int candidateId) {
        return dao.undoSuspension(candidateId);
    }
}
