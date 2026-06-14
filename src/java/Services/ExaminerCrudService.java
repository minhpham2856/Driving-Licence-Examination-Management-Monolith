package Services;

import Models.ExamRegistration;
import Models.User;
import jakarta.servlet.http.HttpSession;

public interface ExaminerCrudService {

    ExamRegistration findCandidate(int sessionId, String sbd);

    boolean updateCandidateProfile(int sessionId, String sbd, String fullName, String dobStr,
            String govIdNo, String email, String phoneNo, String address, String sex, String reasonForTaking,
            HttpSession session);

    boolean markAbsent(int sessionId, String sbd, HttpSession session);

    boolean undoAbsent(int sessionId, String sbd, HttpSession session);

    boolean callCandidate(int sessionId, String sbd, User user, HttpSession session);

    String callNextCandidate(int sessionId, User user, HttpSession session);

    int callSelectedCandidates(int sessionId, String[] sbds, User user, HttpSession session);

    String autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session);

    boolean callScoreEntryCandidate(int sessionId, String sbd, User user, HttpSession session);

    String deferScoreEntryAbsent(int sessionId, String sbd, User user, HttpSession session);

    boolean setDeviceMaintenance(int deviceId, HttpSession session);

    boolean setDeviceAvailable(int deviceId, HttpSession session);

    boolean updateTheoryScore(int sessionId, String sbd, int newScore, String reasonCode,
            String reasonDetail, User user, String password, HttpSession session);

    boolean recordViolation(int sessionId, String sbd, String reasonCode, String reasonDetail,
            String evidencePath, int[] deductionIds, HttpSession session);

    boolean undoSuspension(int sessionId, String sbd, String reasonCode, String reasonDetail,
            HttpSession session);

    boolean verifyPassword(User user, String password);

    boolean printSignatureForm(int sessionId, String sbd, HttpSession session);

    /** null = success; otherwise error code (e.g. needSignaturePrint). */
    String completeCandidateSection(int sessionId, String sbd, HttpSession session);
}
