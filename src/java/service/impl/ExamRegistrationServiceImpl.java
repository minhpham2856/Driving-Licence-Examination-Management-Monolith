package service.impl;

import dao.CandidateDAO;
import dao.ExamEnrollmentDAO;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dto.candidate.CandidateEnrollmentDTO;
import dto.candidate.UploadRecordDTO;
import model.exam.ExamEnrollment;
import service.ExamRegistrationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExamRegistrationServiceImpl implements ExamRegistrationService {

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();

    @Override
    public CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, String sbd) {
        return null;
    }

    @Override
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) {
        return enrollmentDAO.getCandidatesBySession(sessionId);
    }

    @Override
    public boolean updateProfile(int candidateId, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        return false;
    }

    @Override
    public boolean updatePhoto(int candidateId, String photoUrl) {
        return false;
    }

    @Override
    public boolean markAbsent(int candidateId) {
        return false;
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return false;
    }

    @Override
    public boolean markSuspended(int candidateId) {
        return false;
    }

    @Override
    public boolean undoSuspension(int candidateId) {
        return false;
    }

    @Override
    public List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        return new ArrayList<>();
    }

    @Override
    public boolean updateScores(int candidateId, Integer theoryScore, String theoryResult, Integer pracScore, String pracResult) {
        return true;
    }

    @Override
    public boolean updatePresent(int candidateId, boolean isPresent) {
        return true;
    }

    @Override
    public boolean updateAllocatedRoom(int candidateId, int areaId, String areaName) {
        return true;
    }

    @Override
    public boolean insertPayment(model.payment.Payment payment) { return new dao.impl.PaymentDAOImpl().insert(payment); }

    @Override
    public boolean updatePayment(int candidateId, boolean isPaid) {
        return true;
    }

    @Override
    public CandidateEnrollmentDTO getById(int candidateId) {
        return null;
    }

    @Override
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        return null;
    }

    @Override
    public boolean insertProfile(model.user.Profile profile) { return new dao.impl.ProfileDAOImpl().insert(profile); }
    @Override
    public boolean updateProfile(model.user.Profile profile) { return new dao.impl.ProfileDAOImpl().update(profile); }
    @Override
    public model.user.Profile getProfileByGovId(String govId) { return new dao.impl.ProfileDAOImpl().getByGovIdNo(govId); }
    @Override
    public boolean insertUser(model.user.User user) { return new dao.impl.UserDAOImpl().insert(user); }
    @Override
    public model.user.User getUserByUsername(String username) { return new dao.impl.UserDAOImpl().getByUsername(username); }

    @Override
    public boolean insert(UploadRecordDTO dto) {
        return true;
    }

    @Override
    public boolean updateRoadScore(int candidateId, int score, String passed) {
        return true;
    }
}
