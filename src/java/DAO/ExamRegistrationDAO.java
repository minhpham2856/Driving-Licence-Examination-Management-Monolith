package DAO;

import Models.ExamRegistration;
import java.util.List;

public interface ExamRegistrationDAO {
    ExamRegistration getById(int id);
    ExamRegistration getBySessionAndSbd(int sessionId, String sbd);
    List<ExamRegistration> getCandidatesBySession(int sessionId);
    boolean updatePresent(int id, boolean isPresent);
    boolean updatePayment(int id, boolean isPaymentCompleted);
    boolean updateComputer(int id, String computerCode);
    boolean updateAllocatedRoom(int id, int areaId, String areaName);
    boolean updateDevice(int id, String deviceCode);
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);
    boolean updateRoadScore(int id, Integer roadScore, String roadPassed);
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);
    boolean updatePhoto(int id, String photoUrl);
    boolean insert(ExamRegistration reg);
    List<ExamRegistration> getAllCandidates();
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);
}
