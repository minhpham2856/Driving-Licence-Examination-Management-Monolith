package service;

import dto.EnrollmentDTO;
import dto.ServiceResult;
import enums.SectionType;
import model.User;

import java.sql.Date;

public interface CallService {

    void clearPresent(int sessionId, int sbd);

    void markPresent(int sessionId, int sbd);

    boolean isPresent(int sessionId, int sbd);

    void sendToProcedure(int sessionId, int sbd);

    boolean isInProcedureQueue(int sessionId, int sbd);

    void removeCandidate(int sessionId, int sbd);

    EnrollmentDTO getRegistration(int sessionId, int sbd);

    ServiceResult<Void> updateCandidateProfile(int sessionId, int sbd, Integer actionUserId, String fullName,
            Date dateOfBirth, String governmentIdNumber, String phoneNumber, String address, String sex,
            String reasonForTaking);

    ServiceResult<Void> callCandidate(int sessionId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination);

    ServiceResult<Integer> callNextCandidate(int sessionId, User user, Integer actionUserId, SectionType examSection,
            boolean isTheory, String sectionName, String callDestination);

    ServiceResult<Integer> callSelectedCandidates(int sessionId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, int[] sbds);

    ServiceResult<Void> callScoreEntryCandidate(int sessionId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, boolean scoreEntry);

    ServiceResult<Void> adjustScoreDeduction(int sessionId, int sbd, int deductionId, int delta, Integer actionUserId);

    ServiceResult<Void> finalizeScoreEntry(int sessionId, int sbd, Integer actionUserId);

    ServiceResult<Void> setDeviceMaintenance(int deviceId, Integer actionUserId);

    ServiceResult<Void> setDeviceAvailable(int deviceId, Integer actionUserId);

    ServiceResult<Void> changeCandidateVehicle(int sessionId, int sbd, int deviceId, Integer actionUserId);

    ServiceResult<Void> updateTheoryScore(int sessionId, int sbd, User user, String password, Integer newScore,
            String reasonCode, String reasonDetail, Integer actionUserId);

    ServiceResult<Void> logPracticalScoreEditReason(int sessionId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId);

    ServiceResult<Void> recordViolation(int sessionId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath);

    boolean verifyPassword(User user, String password);

    ServiceResult<Void> printSignatureForm(int sessionId, int sbd, Integer actionUserId);

    ServiceResult<Void> markPresent(int sessionId, int sbd, Integer actionUserId);

    ServiceResult<Void> undoPresent(int sessionId, int sbd, Integer actionUserId);

    ServiceResult<Void> sendWrongInfoToProcedure(int sessionId, int sbd, Integer actionUserId);

    ServiceResult<Void> completeCandidateSection(int sessionId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint);
}
