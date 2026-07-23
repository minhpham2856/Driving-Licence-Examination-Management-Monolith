package examiner.service;

import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import shared.enums.SectionType;
import shared.model.User;
import java.util.Map;

// Service contract for examiner call-board actions, presence, scoring, suspensions, and device operations.
public interface ActionService {

    // Loads enrollment details for the given exam and candidate number (SBD).
    EnrollmentDTO getIfByExamAndSbd(int examId, int sbd);

    // Invokes a call-board action for one candidate and logs the procedure action.
    ServiceResult<Void> actionCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination);

    // Invokes the next eligible candidate from the room queue or candidate list.
    ServiceResult<Integer> actionNextCandidate(int examId, int examAreaId, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination);

    // Invokes call-board actions for a batch of selected candidate numbers.
    ServiceResult<Integer> actionSelectedCandidates(int examId, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination, int[] sbds);

    // Opens score entry for one eligible candidate and logs the call action.
    ServiceResult<Void> actionScoreEntryCandidate(int examId, Integer sbd, User user, Integer actionUserId,
            SectionType sectionType, String actionDestination, boolean scoreEntry);

    // Adjusts the occurrence count of a practical score deduction rule during entry.
    ServiceResult<Void> adjustScoreDeduction(int examId, int sbd, int deductionId, int delta,
            Integer actionUserId, SectionType sectionType);

    // Finalizes practical score entry and moves the candidate to awaiting-signature status.
    ServiceResult<Void> finalizeScoreEntry(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    ServiceResult<Void> savePracticalScore(int examId, int examAreaId, int sbd, int deviceId,
            int elapsedSeconds, Map<Integer, Integer> occurrences, Integer actionUserId);

    // Sets an exam device to maintenance status and writes an audit entry.
    ServiceResult<Void> setDeviceMaintenance(int deviceId, Integer actionUserId);

    // Sets an exam device to available status and writes an audit entry.
    ServiceResult<Void> setDeviceAvailable(int deviceId, Integer actionUserId);

    // Assigns a different exam vehicle to a candidate for the active section.
    ServiceResult<Void> changeCandidateVehicle(int examId, int sbd, int deviceId, Integer actionUserId,
            SectionType sectionType);

    // Verifies the examiner password and logs the reason for a practical score edit.
    ServiceResult<Void> logPracticalScoreEditReason(int examId, int sbd, User user, String password,
            String reasonCode, String reasonDetail, Integer actionUserId, SectionType sectionType);

    // Records a violation by suspending the candidate with optional reason metadata.
    ServiceResult<Void> recordViolation(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, String evidencePath, SectionType sectionType);

    // One-click suspend: sets Candidate.IsSuspended=true.
    ServiceResult<Void> markSuspended(int examId, int sbd, Integer actionUserId, String reasonCode,
            String reasonDetail, SectionType sectionType);

    // Clears Candidate.IsSuspended.
    ServiceResult<Void> undoSuspension(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    // Verifies a user's password against the stored hash in the database.
    boolean verifyPassword(User user, String password);

    // Marks the result form as printed and advances theory candidates when applicable.
    ServiceResult<Void> printResultForm(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    // Marks attendance, updates section progress, and dispatches to a room when needed.
    ServiceResult<Void> markPresent(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    // Reverses attendance and rolls back in-progress section status when allowed.
    ServiceResult<Void> undoPresent(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    // Sends a candidate back to the procedure desk after wrong personal information.
    ServiceResult<Void> sendWrongInfoToProcedure(int examId, int sbd, Integer actionUserId, SectionType sectionType);

    // Completes a section after signature, updates dispatch queues, and may advance to layout.
    ServiceResult<Void> completeCandidateSection(int examId, int sbd, Integer actionUserId,
            Boolean sectionPassedHint, SectionType sectionType);

    // Records a procedure-desk action event (replaces branch CandidateCallDAO.insert).
    ServiceResult<Void> recordProcedureAction(int examId, int sbd, String result, String actionDestination,
            Integer actionUserId, SectionType sectionType);
}
