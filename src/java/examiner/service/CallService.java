package examiner.service;

import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import shared.enums.SectionType;
import shared.model.User;

import java.sql.Date;

public interface CallService {

    void clearPresent(int examId, int sbd);

    void markPresent(int examId, int sbd);

    boolean isPresent(int examId, int sbd);

    void sendToProcedure(int examId, int sbd);

    boolean isInProcedureQueue(int examId, int sbd);

    void removeCandidate(int examId, int sbd);

    EnrollmentDTO getRegistration(int examId, int sbd);

    ServiceResult<Void> updateCandidateProfile(int examId, int sbd, Integer actionUserId, String fullName,
            Date dateOfBirth, String governmentIdNumber, String phoneNumber, String address, String sex,
            String reasonForTaking);

    ServiceResult<Void> callCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination);

    ServiceResult<Integer> callNextCandidate(int examId, User user, Integer actionUserId, SectionType examSection,
            boolean isTheory, String sectionName, String callDestination);

    ServiceResult<Integer> callSelectedCandidates(int examId, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, int[] sbds);

    ServiceResult<Void> callScoreEntryCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType examSection, boolean isTheory, String sectionName, String callDestination, boolean scoreEntry);

    ServiceResult<Void> adjustScoreDeduction(int examId, int sbd, int deductionId, int delta, Integer actionUserId);

    ServiceResult<Void> finalizeScoreEntry(int examId, int sbd, Integer actionUserId);

    ServiceResult<Void> setDeviceMaintenance(int deviceId, Integer actionUserId);

    ServiceResult<Void> setDeviceAvailable(int deviceId, Integer actionUserId);

    ServiceResult<Void> changeCandidateVehicle(int examId, int sbd, int deviceId, Integer actionUserId);

    ServiceResult<Void> updateTheoryScore(int examId, int sbd, User user, String password, Integer newScore,
            String reasonCode, String reasonDetail, Integer actionUserId);

    ServiceResult<Void> logPracticalScoreEditReason(int examId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId);

    ServiceResult<Void> recordViolation(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath);

    boolean verifyPassword(User user, String password);

    ServiceResult<Void> printSignatureForm(int examId, int sbd, Integer actionUserId);

    ServiceResult<Void> markPresent(int examId, int sbd, Integer actionUserId);

    ServiceResult<Void> undoPresent(int examId, int sbd, Integer actionUserId);

    ServiceResult<Void> sendWrongInfoToProcedure(int examId, int sbd, Integer actionUserId);

    ServiceResult<Void> completeCandidateSection(int examId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint);

    // Records a procedure-desk call event (replaces branch CandidateCallDAO.insert).
    ServiceResult<Void> recordProcedureCall(int examId, int sbd, String result, String callDestination,
            Integer actionUserId);
}

