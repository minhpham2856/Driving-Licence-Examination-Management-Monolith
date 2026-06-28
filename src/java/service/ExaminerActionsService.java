package service;

import dto.candidate.CandidateEnrollmentDTO;
import enums.SectionType;
import model.user.User;

public interface ExaminerActionsService {

    CandidateEnrollmentDTO findCandidate(int sessionId, String sbd);

    boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            Integer actionUserId);

    boolean markAbsent(int sessionId, String sbd, Integer actionUserId);

    boolean undoAbsent(int sessionId, String sbd, Integer actionUserId);

    boolean callCandidate(int sessionId, String sbd, User user, Integer actionUserId, SectionType sectionType, String sectionName, String callDestination);

    String callNextCandidate(int sessionId, User user, Integer actionUserId, SectionType sectionType, String sectionName, String callDestination);

    int callSelectedCandidates(int sessionId, String[] sbds, User user, Integer actionUserId, SectionType sectionType, String sectionName, String callDestination);

    boolean callScoreEntryCandidate(int sessionId, String sbd, User user, Integer actionUserId, SectionType sectionType, String sectionName, String callDestination);

    boolean adjustScoreDeduction(int sessionId, String sbd, int deductionId, int delta, Integer actionUserId);

    boolean finalizeScoreEntry(int sessionId, String sbd, Integer actionUserId, String sectionKeyword);

    boolean setDeviceMaintenance(int deviceId, Integer actionUserId);

    boolean setDeviceAvailable(int deviceId, Integer actionUserId);

    boolean changeCandidateVehicle(int sessionId, String sbd, int deviceId, Integer actionUserId);

    boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId);

    boolean logPracticalScoreEditReason(int sessionId, String sbd, String reasonCode,
            String reasonDetail, User user, String password, Integer actionUserId);

    boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, Integer actionUserId, SectionType sectionType, String sectionName);

    boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            Integer actionUserId);

    boolean verifyPassword(User user, String password);

    boolean printSignatureForm(int sessionId, String sbd, Integer actionUserId);

    String completeCandidateSection(int sessionId, String sbd, Integer actionUserId);
}

