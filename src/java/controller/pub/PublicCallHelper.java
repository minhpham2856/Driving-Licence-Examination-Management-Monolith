package controller.pub;

import controller.staff.exam.CandidateCallBoard;
import controller.staff.exam.CandidatePhotoHelper;
import controller.staff.exam.ExamStaffViewHelper;
import dao.ExamRegistrationDAO;
import dao.ExamSessionDAO;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.exam.SessionDTO;
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
        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : System.currentTimeMillis();

        if ((nextSbd == null || nextSbd.isBlank()) && !shiftEnded) {
            nextSbd = CandidateCallBoard.resolveNextSbd(queue, callingSbd);
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
