// Forced recompilation trigger
package service.impl;

import dao.ExamRegistrationDAO;
import dao.impl.ExamRegistrationDAOImpl;
import service.ExamRegistrationService;
import dto.exam.ExamRegistrationDTO;
import model.ExamRegistration;
import java.util.List;

public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final ExamRegistrationDAO dao = new ExamRegistrationDAOImpl();

    @Override
    public ExamRegistrationDTO getById(int id) {
        return dao.getById(id);
    }

    @Override
    public ExamRegistration findById(int id) {
        return dao.findById(id);
    }

    @Override
    public ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd) {
        return dao.getBySessionAndSbd(sessionId, sbd);
    }

    @Override
    public List<ExamRegistrationDTO> getCandidatesBySession(int sessionId) {
        return dao.getCandidatesBySession(sessionId);
    }

    @Override
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        return dao.getCandidatesByExam(examId);
    }

    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        return dao.getByExamAndSbd(examId, sbd);
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
    public boolean updateComputer(int id, String computerCode) {
        return dao.updateComputer(id, computerCode);
    }

    @Override
    public boolean updateAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName) {
        return dao.updateAllocatedRoom(candidateId, sessionId, areaId, areaName);
    }

    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName) {
        return dao.updatePracticalAllocatedRoom(candidateId, sessionId, areaId, areaName);
    }

    @Override
    public String validateUniqueTheoryAllocation(int candidateId, int sessionId) {
        return dao.validateUniqueTheoryAllocation(candidateId, sessionId);
    }

    @Override
    public boolean updateDevice(int id, String deviceCode) {
        return dao.updateDevice(id, deviceCode);
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    @Override
    public boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        return dao.updateScores(id, sessionId, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    @Override
    public boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold) {
        return dao.updateTheoryCorrectCount(id, correctCount, passThreshold);
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
    public boolean insert(ExamRegistrationDTO reg) {
        return dao.insert(reg);
    }

    @Override
    public List<ExamRegistrationDTO> getAllCandidates() {
        return dao.getAllCandidates();
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
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        return dao.findCandidateIdByProfileAndSession(profileId, sessionId);
    }

    @Override
    public Integer findCandidateIdByGovIdAndSession(String govId, int sessionId) {
        return dao.findCandidateIdByGovIdAndSession(govId, sessionId);
    }

    @Override
    public boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword) {
        return dao.applyScoreDeductions(candidateId, deductionIds, sectionKeyword);
    }

    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta) {
        return dao.adjustScoreDeductionOccurrence(candidateId, sessionId, deductionId, delta);
    }

    @Override
    public boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword) {
        return dao.finalizeScoreEntry(candidateId, sessionId, sectionKeyword);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        return dao.findAppliedScoreDeductions(candidateId, sessionId);
    }

    @Override
    public boolean markSuspended(int candidateId) {
        return dao.markSuspended(candidateId);
    }

    @Override
    public boolean undoSuspension(int candidateId) {
        return dao.undoSuspension(candidateId);
    }

    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        dao.syncSectionStatusesForSession(sessionId);
    }

    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        return dao.markSignaturePrinted(candidateId, sessionId);
    }

    @Override
    public boolean completeSection(int candidateId, int sessionId) {
        return dao.completeSection(candidateId, sessionId);
    }
}


