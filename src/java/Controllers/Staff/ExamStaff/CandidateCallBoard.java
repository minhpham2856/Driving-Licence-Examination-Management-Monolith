package Controllers.Staff.ExamStaff;

import DTOs.ExamRegistrationDTO;
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

        public int getExamSessionId() {
            return examSessionId;
        }

        public void setExamSessionId(int examSessionId) {
            this.examSessionId = examSessionId;
        }

        public String getCallingSbd() {
            return callingSbd;
        }

        public void setCallingSbd(String callingSbd) {
            this.callingSbd = callingSbd;
        }

        public String getNextSbd() {
            return nextSbd;
        }

        public void setNextSbd(String nextSbd) {
            this.nextSbd = nextSbd;
        }

        public boolean isShiftEnded() {
            return shiftEnded;
        }

        public void setShiftEnded(boolean shiftEnded) {
            this.shiftEnded = shiftEnded;
        }

        public long getUpdatedAtMs() {
            return updatedAtMs;
        }

        public void setUpdatedAtMs(long updatedAtMs) {
            this.updatedAtMs = updatedAtMs;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, State> getBoards(ServletContext ctx) {
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
        return active != null ? active : 2;
    }

    public static void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        State state = getBoards(ctx).computeIfAbsent(examSessionId, id -> new State());
        state.setExamSessionId(examSessionId);
        state.setCallingSbd(emptyToNull(callingSbd));
        state.setNextSbd(resolveNextSbd(queue, state.getCallingSbd()));
        state.setShiftEnded(shiftEnded);
        state.setUpdatedAtMs(System.currentTimeMillis());
        ctx.setAttribute(ACTIVE_SESSION_KEY, examSessionId);
    }

    public static void syncFromSession(ServletContext ctx, HttpSession session,
            List<ExamRegistrationDTO> queue) {
        if (ctx == null || session == null) {
            return;
        }
        int examSessionId = resolveActiveSessionId(ctx, session, null);
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
            if (isProcedureDone(c)) {
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

    private static boolean isProcedureDone(ExamRegistrationDTO c) {
        return c.isPaymentCompleted() && c.isValidCapturedPhoto();
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
