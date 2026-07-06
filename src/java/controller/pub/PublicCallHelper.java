package controller.pub;

import controller.staff.exam.CandidateCallBoard;
import controller.staff.exam.CandidatePhotoHelper;
import controller.staff.exam.ExamStaffViewHelper;
import dao.ExamRegistrationDAO;
import dao.ExamSessionDAO;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.SessionDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

public final class PublicCallHelper {

    public static final class Snapshot {
        private int sessionId;
        private SessionDTO currentSession;
        private ExamRegistrationDTO callingCandidate;
        private ExamRegistrationDTO nextCandidate;
        private List<ExamRegistrationDTO> waitingQueue;
        private boolean callingActive;
        private boolean shiftEnded;
        private long updatedAtMs;
        private boolean deskBusy;
        private String deskSbd;

        public int getSessionId() {
            return sessionId;
        }

        public SessionDTO getCurrentSession() {
            return currentSession;
        }

        public ExamRegistrationDTO getCallingCandidate() {
            return callingCandidate;
        }

        public ExamRegistrationDTO getNextCandidate() {
            return nextCandidate;
        }

        public List<ExamRegistrationDTO> getWaitingQueue() {
            return waitingQueue;
        }

        public boolean isCallingActive() {
            return callingActive;
        }

        public boolean isShiftEnded() {
            return shiftEnded;
        }

        public long getUpdatedAtMs() {
            return updatedAtMs;
        }

        public boolean isDeskBusy() {
            return deskBusy;
        }

        public String getDeskSbd() {
            return deskSbd;
        }
    }

    private PublicCallHelper() {
    }

    public static Snapshot loadSnapshot(HttpServletRequest request) {
        int sessionId = CandidateCallBoard.resolveActiveSessionId(
                request.getServletContext(),
                request.getSession(false),
                request.getParameter("sessionId"));

        Snapshot snapshot = new Snapshot();
        snapshot.sessionId = sessionId;
        snapshot.waitingQueue = new ArrayList<>();
        snapshot.updatedAtMs = System.currentTimeMillis();

        if (sessionId <= 0) {
            return snapshot;
        }

        ServletContext ctx = request.getServletContext();
        List<ExamRegistrationDTO> queue = loadQueue(sessionId);
        CandidatePhotoHelper.normalizeQueue(ctx.getRealPath("/"), queue);

        CandidateCallBoard.State board = CandidateCallBoard.getState(ctx, sessionId);
        if (board != null && board.getQueueOrderSbds() != null && !board.getQueueOrderSbds().isEmpty()) {
            queue = CandidateCallBoard.applyQueueOrder(queue, board.getQueueOrderSbds());
        }
        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        boolean deskBusy = board != null && board.isDeskBusy();
        String deskSbd = board != null ? board.getDeskSbd() : null;
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : System.currentTimeMillis();

        if ((nextSbd == null || nextSbd.isBlank()) && !shiftEnded) {
            if (deskBusy && deskSbd != null && !deskSbd.isBlank()) {
                nextSbd = ExamStaffViewHelper.resolveNextCallingSbd(queue, deskSbd);
            } else {
                nextSbd = CandidateCallBoard.resolveNextSbd(queue, callingSbd);
            }
        }

        ExamRegistrationDTO callingCandidate = CandidateCallBoard.findBySbd(queue, callingSbd);
        ExamRegistrationDTO nextCandidate = CandidateCallBoard.findBySbd(queue, nextSbd);

        SessionDTO currentSession = loadSession(sessionId);

        snapshot.currentSession = currentSession;
        snapshot.callingCandidate = callingCandidate;
        snapshot.nextCandidate = nextCandidate;
        snapshot.waitingQueue = listWaitingTop(queue, 10);
        snapshot.callingActive = callingCandidate != null && !shiftEnded;
        snapshot.shiftEnded = shiftEnded;
        snapshot.updatedAtMs = updatedAtMs;
        snapshot.deskBusy = deskBusy;
        snapshot.deskSbd = deskSbd;
        return snapshot;
    }

    private static List<ExamRegistrationDTO> loadQueue(int sessionId) {
        ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
        try {
            return regDAO.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<ExamRegistrationDTO> listWaitingTop(List<ExamRegistrationDTO> queue, int limit) {
        List<ExamRegistrationDTO> result = new ArrayList<>();
        if (queue == null || limit <= 0) {
            return result;
        }
        for (ExamRegistrationDTO c : queue) {
            if (ExamStaffViewHelper.isCallablePending(c)) {
                result.add(c);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    private static SessionDTO loadSession(int sessionId) {
        ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
        try {
            return sessionDAO.getById(sessionId);
        } catch (Exception e) {
            return null;
        }
    }
}
