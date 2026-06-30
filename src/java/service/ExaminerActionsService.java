package service;

import dto.CandidateEnrollmentDTO;
import enums.SectionType;
import model.User;

public interface ExaminerActionsService {

    CandidateEnrollmentDTO findCandidate(int sessionId, int sbd);

    boolean updateCandidateProfile(int sessionId, int sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            Integer actionUserId);

    boolean markAbsent(int sessionId, int sbd, Integer actionUserId);

    boolean undoAbsent(int sessionId, int sbd, Integer actionUserId);

    boolean callCandidate(int sessionId, int sbd, User user, Integer actionUserId, SectionType sectionType,
            String sectionName, String callDestination);

    Integer callNextCandidate(int sessionId, User user, Integer actionUserId, SectionType sectionType,
            String sectionName, String callDestination);

    int callSelectedCandidates(int sessionId, int[] sbds, User user, Integer actionUserId, SectionType sectionType,
            String sectionName, String callDestination);

    boolean callScoreEntryCandidate(int sessionId, int sbd, User user, Integer actionUserId, SectionType sectionType,
            String sectionName, String callDestination);

    boolean adjustScoreDeduction(int sessionId, int sbd, int deductionId, int delta, Integer actionUserId);

    boolean finalizeScoreEntry(int sessionId, int sbd, Integer actionUserId, String sectionKeyword);

    boolean setDeviceMaintenance(int deviceId, Integer actionUserId);

    boolean setDeviceAvailable(int deviceId, Integer actionUserId);

    boolean changeCandidateVehicle(int sessionId, int sbd, int deviceId, Integer actionUserId);

    boolean updateTheoryScore(int sessionId, int sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId);

    boolean logPracticalScoreEditReason(int sessionId, int sbd, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId);

    boolean recordViolation(int sessionId, int sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, Integer actionUserId, SectionType sectionType, String sectionName);

    boolean undoSuspension(int sessionId, int sbd, String reasonCode, String reasonDetail,
            Integer actionUserId);

    boolean verifyPassword(User user, String password);

    boolean printSignatureForm(int sessionId, int sbd, Integer actionUserId);

    String completeCandidateSection(int sessionId, int sbd, Integer actionUserId);
}
