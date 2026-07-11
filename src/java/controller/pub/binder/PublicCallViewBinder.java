package controller.pub.binder;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.PublicCallSnapshotDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import util.examstaff.LicenseClassRules;

public final class PublicCallViewBinder {

    private PublicCallViewBinder() {
    }

    private static String normalizeLicenseForPublicCall(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static void normalizeSession(SessionDTO s) {
        if (s == null) return;
        s.setLicenseCode(normalizeLicenseForPublicCall(s.getLicenseCode()));
    }

    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) return;
        c.setLicenseCode(normalizeLicenseForPublicCall(c.getLicenseCode()));
    }

    public static void bind(HttpServletRequest request, PublicCallSnapshotDTO snapshot) {
        if (request == null || snapshot == null) {
            return;
        }
        normalizeSession(snapshot.getCurrentSession());
        normalizeCandidate(snapshot.getCallingCandidate());
        normalizeCandidate(snapshot.getNextCandidate());
        if (snapshot.getWaitingQueue() != null) {
            for (ExamRegistrationDTO c : snapshot.getWaitingQueue()) {
                normalizeCandidate(c);
            }
        }

        boolean hasSession = snapshot.getSessionId() > 0;
        request.setAttribute("noActiveSession", !hasSession);
        request.setAttribute("sessionId", hasSession ? snapshot.getSessionId() : null);
        request.setAttribute("currentSession", snapshot.getCurrentSession());
        request.setAttribute("callingCandidate", snapshot.getCallingCandidate());
        request.setAttribute("nextCandidate", snapshot.getNextCandidate());
        request.setAttribute("isCallingActive", snapshot.isCallingActive());
        request.setAttribute("shiftEnded", snapshot.isShiftEnded());
        request.setAttribute("examPaused", snapshot.isExamPaused());
        request.setAttribute("waitingQueue", snapshot.getWaitingQueue());
    }
}
