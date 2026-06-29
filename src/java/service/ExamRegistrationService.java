package service;

import java.sql.*;

import model.*;

import dto.CandidateEnrollmentDTO;
import dto.UploadRecordDTO;
import java.util.List;
import java.util.Map;

public interface ExamRegistrationService {
    CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, String sbd);
    List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId);
    
    boolean updateProfile(int candidateId, String fullName, Date dob, String govIdNo, String email, String phoneNo);
    boolean updatePhoto(int candidateId, String photoUrl);
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    
    boolean markSuspended(int candidateId);
    boolean undoSuspension(int candidateId);
    
    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    boolean updateScores(int candidateId, Integer theoryScore, String theoryResult, Integer pracScore, String pracResult);
    boolean updatePresent(int candidateId, boolean isPresent);
    
    boolean updateAllocatedRoom(int candidateId, int areaId, String areaName);
    boolean updatePayment(int candidateId, boolean isPaid);
    boolean insertPayment(Payment payment);
    CandidateEnrollmentDTO getById(int candidateId);
    
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);
    boolean insert(UploadRecordDTO dto);
    boolean insertProfile(Profile profile);
    boolean updateProfile(Profile profile);
    Profile getProfileByGovId(String govId);
    boolean insertUser(User user);
    User getUserByUsername(String username);
    boolean updateRoadScore(int candidateId, int score, String passed);
}
