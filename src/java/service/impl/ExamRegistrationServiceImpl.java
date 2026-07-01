package service.impl;
import dao.CandidateDAO;
import dao.ExamEnrollmentDAO;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.PaymentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import dto.CandidateEnrollmentDTO;
import dto.UploadRecordDTO;
import model.ExamEnrollment;
import model.Payment;
import model.Profile;
import model.User;
import service.ExamRegistrationService;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class ExamRegistrationServiceImpl implements ExamRegistrationService {
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final CandidateEnrollmentViewSupport enrollmentViewSupport = new CandidateEnrollmentViewSupport();
    @Override
    public CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, int sbd) {
        if (sessionId <= 0 || sbd <= 0) {
            return null;
        }
        for (CandidateEnrollmentDTO row : getCandidatesBySession(sessionId)) {
            if (row.getSbd() == sbd) {
                return row;
            }
        }
        return null;
    }
    @Override
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) {
        return enrollmentViewSupport.getCandidatesBySession(sessionId);
    }
    @Override
    public boolean updateProfile(int candidateId, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
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
    public boolean insertPayment(Payment payment) {
        return new PaymentDAOImpl().insert(payment);
    }
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
    public boolean insertProfile(Profile profile) {
        return new ProfileDAOImpl().insert(profile);
    }
    @Override
    public boolean updateProfile(Profile profile) {
        return new ProfileDAOImpl().update(profile);
    }
    @Override
    public Profile getProfileByGovId(String govId) {
        return new ProfileDAOImpl().getByGovIdNo(govId);
    }
    @Override
    public boolean insertUser(User user) {
        return new UserDAOImpl().insert(user);
    }
    @Override
    public User getUserByUsername(String username) {
        return new UserDAOImpl().getByUsername(username);
    }
    @Override
    public boolean insert(UploadRecordDTO dto) {
        return true;
    }
    @Override
    public boolean updateRoadScore(int candidateId, int score, String passed) {
        return true;
    }
}
