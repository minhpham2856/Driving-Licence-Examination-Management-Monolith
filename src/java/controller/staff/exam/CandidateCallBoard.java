package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
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
        private List<String> queueOrderSbds = new ArrayList<>();
        private boolean deskBusy;
        private String deskSbd;

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

        public List<String> getQueueOrderSbds() {
            return queueOrderSbds;
        }

        public void setQueueOrderSbds(List<String> queueOrderSbds) {
            this.queueOrderSbds = queueOrderSbds != null ? new ArrayList<>(queueOrderSbds) : new ArrayList<>();
        }

        public boolean isDeskBusy() {
            return deskBusy;
        }

        public void setDeskBusy(boolean deskBusy) {
            this.deskBusy = deskBusy;
        }

        public String getDeskSbd() {
            return deskSbd;
        }

        public void setDeskSbd(String deskSbd) {
            this.deskSbd = deskSbd;
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
        boolean wasDeskBusy = state.isDeskBusy();
        String wasDeskSbd = state.getDeskSbd();

        state.setExamSessionId(examSessionId);
        if (!wasDeskBusy) {
            state.setCallingSbd(emptyToNull(callingSbd));
        }
        if (wasDeskBusy && wasDeskSbd != null && !wasDeskSbd.isBlank()) {
            state.setNextSbd(ExamStaffViewHelper.resolveNextCallingSbd(queue, wasDeskSbd));
        } else {
            state.setNextSbd(resolveNextSbd(queue, state.getCallingSbd()));
        }
        state.setQueueOrderSbds(extractQueueOrder(queue));
        state.setShiftEnded(shiftEnded);
        state.setDeskBusy(wasDeskBusy);
        state.setDeskSbd(wasDeskSbd);
        state.setUpdatedAtMs(System.currentTimeMillis());
        ctx.setAttribute(ACTIVE_SESSION_KEY, examSessionId);
    }

    /** Thí sinh đang làm thủ tục tại bàn — loa chuyển sang gọi chuẩn bị người kế tiếp. */
    public static void occupyDesk(ServletContext ctx, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        State state = getBoards(ctx).computeIfAbsent(examSessionId, id -> new State());
        state.setExamSessionId(examSessionId);
        state.setDeskBusy(true);
        state.setDeskSbd(emptyToNull(deskSbd));
        if (state.getCallingSbd() == null || state.getCallingSbd().isBlank()) {
            state.setCallingSbd(state.getDeskSbd());
        }
        state.setNextSbd(ExamStaffViewHelper.resolveNextCallingSbd(queue, deskSbd));
        state.setQueueOrderSbds(extractQueueOrder(queue));
        state.setShiftEnded(shiftEnded);
        state.setUpdatedAtMs(System.currentTimeMillis());
        ctx.setAttribute(ACTIVE_SESSION_KEY, examSessionId);
    }

    /** Kết thúc thủ tục tại bàn — gọi thí sinh tiếp theo vào bàn. */
    public static void releaseDeskAndCall(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        State state = getBoards(ctx).computeIfAbsent(examSessionId, id -> new State());
        state.setExamSessionId(examSessionId);
        state.setDeskBusy(false);
        state.setDeskSbd(null);
        state.setCallingSbd(emptyToNull(callingSbd));
        state.setNextSbd(resolveNextSbd(queue, state.getCallingSbd()));
        state.setQueueOrderSbds(extractQueueOrder(queue));
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
        if (examSessionId <= 0) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
        sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    public static String resolveNextSbd(List<ExamRegistrationDTO> queue, String callingSbd) {
        return ExamStaffViewHelper.resolveNextCallingSbd(queue, callingSbd);
    }

    private static List<String> extractQueueOrder(List<ExamRegistrationDTO> queue) {
        List<String> order = new ArrayList<>();
        if (queue == null) {
            return order;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null && !c.getSbd().isBlank()) {
                order.add(c.getSbd());
            }
        }
        return order;
    }

    public static List<ExamRegistrationDTO> applyQueueOrder(List<ExamRegistrationDTO> queue,
            List<String> orderSbds) {
        if (queue == null || queue.isEmpty() || orderSbds == null || orderSbds.isEmpty()) {
            return queue;
        }
        Map<String, ExamRegistrationDTO> bySbd = new java.util.LinkedHashMap<>();
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null) {
                bySbd.put(c.getSbd(), c);
            }
        }
        List<ExamRegistrationDTO> reordered = new ArrayList<>();
        for (String sbd : orderSbds) {
            ExamRegistrationDTO c = bySbd.remove(sbd);
            if (c != null) {
                reordered.add(c);
            }
        }
        reordered.addAll(bySbd.values());
        return reordered;
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
