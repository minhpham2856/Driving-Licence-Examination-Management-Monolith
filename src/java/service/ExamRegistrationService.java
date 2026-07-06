package service;

import dto.CandidateEnrollmentDTO;
import dto.ServiceResult;
import dto.UploadRecordDTO;
import dto.payload.UpdateAllocatedRoomCommand;
import dto.payload.UpdateEnrollmentProfileCommand;
import dto.payload.UpdateEnrollmentScoresCommand;
import dto.payload.UpdateRoadScoreCommand;
import model.Payment;
import model.Profile;
import model.User;

import java.util.List;
import java.util.Map;

public interface ExamRegistrationService {

    CandidateEnrollmentDTO getBySessionAndSbd(int sessionId, int sbd);

    List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId);

    ServiceResult<Void> updateProfile(UpdateEnrollmentProfileCommand command);

    ServiceResult<Void> updatePhoto(int candidateId, String photoUrl);

    ServiceResult<Void> markAbsent(int candidateId);

    ServiceResult<Void> clearAbsentMarking(int candidateId);

    ServiceResult<Void> markSuspended(int candidateId);

    ServiceResult<Void> undoSuspension(int candidateId);

    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    ServiceResult<Void> updateScores(UpdateEnrollmentScoresCommand command);

    ServiceResult<Void> updatePresent(int candidateId, boolean isPresent);

    ServiceResult<Void> updateAllocatedRoom(UpdateAllocatedRoomCommand command);

    ServiceResult<Void> updatePayment(int candidateId, boolean isPaid);

    boolean insertPayment(Payment payment);

    CandidateEnrollmentDTO getById(int candidateId);

    Integer findCandidateIdByGovIdAndSession(String governmentIdNumber, int sessionId);

    ServiceResult<Void> insert(UploadRecordDTO dto);

    boolean insertProfile(Profile profile);

    boolean updateProfile(Profile profile);

    Profile getProfileByGovId(String govId);

    boolean insertUser(User user);

    User getUserByUsername(String username);

    ServiceResult<Void> updateRoadScore(UpdateRoadScoreCommand command);
}
