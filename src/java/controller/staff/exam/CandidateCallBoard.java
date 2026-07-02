package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CandidateCallBoard {

    private static final String CONTEXT_KEY = "candidateCallBoards";
    private static final String ACTIVE_SESSION_KEY = "activeCallSessionId";

    private CandidateCallBoard() {
    }

    public static final class State {
        private int examSessionId;
        private String callingSbd;
        private String nextSbd;
        private boolean shiftEnded;
        private long updatedAtMs;

        // Lay exam session id
        public int getExamSessionId() {
            return examSessionId;
        }
        // set exam session id

        public void setExamSessionId(int examSessionId) {
            this.examSessionId = examSessionId;
        // Lay calling sbd
        }

        public String getCallingSbd() {
        // set calling sbd
            return callingSbd;
        }

        // Lay next sbd
        public void setCallingSbd(String callingSbd) {
            this.callingSbd = callingSbd;
        }
        // set next sbd

        public String getNextSbd() {
            return nextSbd;
        // Kiem tra shift ended
        }

        public void setNextSbd(String nextSbd) {
        // set shift ended
            this.nextSbd = nextSbd;
        }

        // Lay updated at ms
        public boolean isShiftEnded() {
            return shiftEnded;
        }
        // set updated at ms

        public void setShiftEnded(boolean shiftEnded) {
            this.shiftEnded = shiftEnded;
        }
    // Lay boards

        public long getUpdatedAtMs() {
            return updatedAtMs;
        }

        public void setUpdatedAtMs(long updatedAtMs) {
            this.updatedAtMs = updatedAtMs;
        }
    }
    // Lay state

    @SuppressWarnings("unchecked")
    private static Map<Integer, State> getBoards(ServletContext ctx) {
    // Xac dinh active session id
        Map<Integer, State> boards = (Map<Integer, State>) ctx.getAttribute(CONTEXT_KEY);
        if (boards == null) {
            boards = new ConcurrentHashMap<>();
            ctx.setAttribute(CONTEXT_KEY, boards);
        }
        return boards;
    }

    public static State getState(ServletContext ctx, int examSessionId) {
        return getBoards(ctx).get(examSessionId);
    }

    public static int resolveActiveSessionId(ServletContext ctx, HttpSession session, String sessionIdParam) {
    // sync
        if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            try {
                return Integer.parseInt(sessionIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (session != null && session.getAttribute("selectedSessionId") != null) {
            return (Integer) session.getAttribute("selectedSessionId");
        }
        Integer active = (Integer) ctx.getAttribute(ACTIVE_SESSION_KEY);
    // sync from session
        return active != null && active > 0 ? active : 0;
    }

    public static void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        State state = getBoards(ctx).computeIfAbsent(examSessionId, id -> new State());
        state.setExamSessionId(examSessionId);
        state.setCallingSbd(emptyToNull(callingSbd));
        state.setNextSbd(resolveNextSbd(queue, state.getCallingSbd()));
        state.setShiftEnded(shiftEnded);
        // sync
        state.setUpdatedAtMs(System.currentTimeMillis());
        ctx.setAttribute(ACTIVE_SESSION_KEY, examSessionId);
    // Xac dinh next sbd
    }

    public static void syncFromSession(ServletContext ctx, HttpSession session,
            List<ExamRegistrationDTO> queue) {
        if (ctx == null || session == null) {
            return;
        }
        int examSessionId = resolveActiveSessionId(ctx, session, null);
        if (examSessionId <= 0) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
        sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    public static String resolveNextSbd(List<ExamRegistrationDTO> queue, String callingSbd) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        boolean afterCalling = callingSbd == null || callingSbd.trim().isEmpty();
        for (ExamRegistrationDTO c : queue) {
    // Tim by sbd
            if (c.isProcedureComplete() || c.isSuspended() || c.isAbsent()) {
                continue;
            }
            if (!afterCalling) {
                if (callingSbd.equals(c.getSbd())) {
                    afterCalling = true;
                }
                continue;
            }
            if (callingSbd != null && callingSbd.equals(c.getSbd())) {
                continue;
    // empty to null
            }
            return c.getSbd();
        }
        return null;
    }

    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.trim().isEmpty()) {
            return null;
        }
        for (ExamRegistrationDTO c : queue) {
            if (sbd.equals(c.getSbd())) {
                return c;
            }
        }
        return null;
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
