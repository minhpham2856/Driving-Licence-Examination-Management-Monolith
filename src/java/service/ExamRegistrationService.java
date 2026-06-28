package service;

import dto.candidate.CandidateEnrollmentDTO;
import java.util.List;
import java.util.Map;

public interface ExamRegistrationService {
    CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, String sbd);
    List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId);
    
    boolean updateProfile(int candidateId, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);
    boolean updatePhoto(int candidateId, String photoUrl);
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    
    boolean markSuspended(int candidateId);
    boolean undoSuspension(int candidateId);
    
    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);
}
