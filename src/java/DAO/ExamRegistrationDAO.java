package DAO;

import Models.ExamRegistration;
import java.util.List;

public interface ExamRegistrationDAO {
    ExamRegistration getById(int id);
    ExamRegistration getBySessionAndSbd(int sessionId, String sbd);
    ExamRegistration getBySbd(String sbd);
    List<ExamRegistration> getCandidatesBySession(int sessionId);
    List<ExamRegistration> getCandidatesByExamId(int examId);
    boolean updatePresent(int id, boolean isPresent);
    boolean updatePayment(int id, boolean isPaymentCompleted);
    boolean updatePayment(int id, boolean isPaymentCompleted, double totalAmount);
    boolean updateComputer(int id, String computerCode);
    boolean updateAllocatedRoom(int id, int areaId, String areaName);
    boolean clearAllocatedRoom(int candidateId);
    boolean updateDevice(int id, String deviceCode);
    boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    boolean updateScoresForExam(int id, int examId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    boolean updateRoadScore(int id, int sessionId, Integer roadScore, String roadPassed);

    boolean updateRoadScoreForExam(int id, int examId, Integer roadScore, String roadPassed);

    Integer resolveSessionIdForSection(int examId, String sectionKeyword);
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);
    boolean updatePhoto(int id, String photoUrl);
    boolean insert(ExamRegistration reg);
    List<ExamRegistration> getAllCandidates();
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);
}
