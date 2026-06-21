package Services;

import DTOs.CandidateDTO;
import Models.User;
import jakarta.servlet.http.HttpSession;

 // Service interface for examiner CRUD operations during an active exam session.
public interface ExaminerService {

    // Looks up a candidate by SBD within the given session (returns null if not found)
    CandidateDTO findCandidate(int sessionId, String sbd);

    // Updates a candidate's personal profile fields (name, DOB, gov ID, contact info, etc.)
    boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            HttpSession session);

    // Records a candidate as absent for the current exam session
    boolean markAbsent(int sessionId, String sbd, HttpSession session);

    // Marks absent during score-entry mode — clears active SBD and returns the next SBD in queue
    String markAbsentInScoreEntry(int sessionId, String sbd, HttpSession session);

    // Reverses a previous absence marking and restores the candidate to active status
    boolean undoAbsent(int sessionId, String sbd, HttpSession session);

    // Calls (summons) a specific candidate to the examiner's station by SBD
    boolean callCandidate(int sessionId, String sbd, User user, HttpSession session);

    // Auto-advances to the next eligible candidate in the queue and calls them
    String callNextCandidate(int sessionId, User user, HttpSession session);

    // Batch-calls multiple selected candidates at once, returns count of successfully called
    int callSelectedCandidates(int sessionId, String[] sbds, User user, HttpSession session);

    // Auto-calls the next score-entry candidate if the queue conditions are met
    String autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session);

    // Calls a specific candidate for score entry (practical/road exam)
    boolean callScoreEntryCandidate(int sessionId, String sbd, User user, HttpSession session);

    // Defers a candidate in score-entry mode: marks absent temporarily and calls the next one
    String deferScoreEntryAbsent(int sessionId, String sbd, User user, HttpSession session);

    // Adjusts a score deduction count (+1 or -1) for a candidate during practical scoring
    boolean adjustScoreDeduction(int sessionId, String sbd, int deductionId, int delta, HttpSession session);

    // Finalises score entry: recalculates the total score and marks the section as awaiting signature
    boolean finalizeScoreEntry(int sessionId, String sbd, HttpSession session);

    // Puts a device into maintenance mode (unavailable for exams)
    boolean setDeviceMaintenance(int deviceId, HttpSession session);

    // Restores a device to available mode (ready for exam use)
    boolean setDeviceAvailable(int deviceId, HttpSession session);

    // Changes the vehicle/device assigned to a candidate for practical exams
    boolean changeCandidateVehicle(int sessionId, String sbd, int deviceId, HttpSession session);

    // Updates the theory score with password verification (audit-protected operation)
    boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session);

    // Logs an audit trail entry for a practical score edit reason (audit-protected)
    boolean logPracticalScoreEditReason(int sessionId, String sbd, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session);

    // Records a violation with evidence and applies score deductions to the candidate
    boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, HttpSession session);

    // Reverses a candidate suspension and restores their active exam status
    boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            HttpSession session);

    // Verifies that the given plain-text password matches the user's stored hash
    boolean verifyPassword(User user, String password);

    // Prints the signature form for a candidate and marks it as printed in the system
    boolean printSignatureForm(int sessionId, String sbd, HttpSession session);

    // Completes the current exam section for a candidate; returns null on success, error code on failure
    String completeCandidateSection(int sessionId, String sbd, HttpSession session);
}
