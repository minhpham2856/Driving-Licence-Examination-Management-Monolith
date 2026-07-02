package controller.examiner;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Hàng đợi nhập điểm theo ca (session) — lưu thứ tự SBD trong HttpSession.
 */
public final class ExaminerScoreEntryQueue {

    private ExaminerScoreEntryQueue() {
    }

    private static String queueKey(int sessionId) {
        return "examinerScoreQueue_" + sessionId;
    }

    private static String activeKey(int sessionId) {
        return "examinerScoreActiveSbd_" + sessionId;
    }

    private static String calledKey(int sessionId) {
        return "examinerScoreCalledSbd_" + sessionId;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getQueue(HttpSession session, int sessionId) {
        if (session == null) {
            return List.of();
        }
        Object value = session.getAttribute(queueKey(sessionId));
        if (value instanceof List<?> list) {
            List<String> copy = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    copy.add(String.valueOf(item));
                }
            }
            return copy;
        }
        return new ArrayList<>();
    }

    public static void syncQueue(HttpSession session, int sessionId, List<String> eligibleSbds) {
        if (session == null || eligibleSbds == null) {
            return;
        }
        Set<String> eligible = new LinkedHashSet<>(eligibleSbds);
        List<String> current = getQueue(session, sessionId);
        List<String> merged = new ArrayList<>();
        for (String sbd : current) {
            if (eligible.contains(sbd)) {
                merged.add(sbd);
                eligible.remove(sbd);
            }
        }
        merged.addAll(eligible);
        session.setAttribute(queueKey(sessionId), merged);

        String active = getActiveSbd(session, sessionId);
        if (active != null && !merged.contains(active)) {
            session.removeAttribute(activeKey(sessionId));
        }
        String called = getCalledSbd(session, sessionId);
        if (called != null && !merged.contains(called)) {
            session.removeAttribute(calledKey(sessionId));
        }
    }

    public static String getActiveSbd(HttpSession session, int sessionId) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(activeKey(sessionId));
        return value != null ? String.valueOf(value) : null;
    }

    public static void setActiveSbd(HttpSession session, int sessionId, String sbd) {
        if (session == null || sbd == null || sbd.isBlank()) {
            return;
        }
        session.setAttribute(activeKey(sessionId), sbd.trim());
    }

    public static String getCalledSbd(HttpSession session, int sessionId) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(calledKey(sessionId));
        return value != null ? String.valueOf(value) : null;
    }

    public static void setCalledSbd(HttpSession session, int sessionId, String sbd) {
        if (session == null) {
            return;
        }
        if (sbd == null || sbd.isBlank()) {
            session.removeAttribute(calledKey(sessionId));
            return;
        }
        session.setAttribute(calledKey(sessionId), sbd.trim());
    }

    public static String firstInQueue(HttpSession session, int sessionId) {
        List<String> queue = getQueue(session, sessionId);
        return queue.isEmpty() ? null : queue.get(0);
    }

    public static String moveToBottom(HttpSession session, int sessionId, String sbd) {
        if (session == null || sbd == null || sbd.isBlank()) {
            return firstInQueue(session, sessionId);
        }
        List<String> queue = new ArrayList<>(getQueue(session, sessionId));
        String normalized = sbd.trim();
        int idx = queue.indexOf(normalized);
        if (idx >= 0) {
            queue.remove(idx);
            queue.add(normalized);
            session.setAttribute(queueKey(sessionId), queue);
        }
        session.removeAttribute(calledKey(sessionId));
        if (normalized.equals(getActiveSbd(session, sessionId))) {
            session.removeAttribute(activeKey(sessionId));
        }
        return queue.isEmpty() ? null : queue.get(0);
    }
}
