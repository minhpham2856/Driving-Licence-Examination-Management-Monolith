package Controllers.Public;

import Controllers.Staff.ExamStaff.CandidateCallBoard;
import Controllers.Staff.ExamStaff.CandidatePhotoHelper;
import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import Models.ExamRegistration;
import Models.ExamSession;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public final class PublicCallHelper {

    public static final class Snapshot {
        private int sessionId;
        private ExamSession currentSession;
        private ExamRegistration callingCandidate;
        private ExamRegistration nextCandidate;
        private boolean callingActive;
        private boolean shiftEnded;
        private long updatedAtMs;

        public int getSessionId() {
            return sessionId;
        }

        public ExamSession getCurrentSession() {
            return currentSession;
        }

        public ExamRegistration getCallingCandidate() {
            return callingCandidate;
        }

        public ExamRegistration getNextCandidate() {
            return nextCandidate;
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
        ServletContext ctx = request.getServletContext();
        HttpSession session = request.getSession(false);
        int sessionId = CandidateCallBoard.resolveActiveSessionId(
                ctx, session, request.getParameter("sessionId"));

        List<ExamRegistration> queue = new ArrayList<>();
        ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
        try {
            queue = regDAO.getCandidatesBySession(sessionId);
        } catch (Exception ignored) {
        }
        CandidatePhotoHelper.normalizeQueue(ctx.getRealPath("/"), queue, regDAO);

        CandidateCallBoard.State board = CandidateCallBoard.getState(ctx, sessionId);
        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : 0L;

        if (nextSbd == null && !shiftEnded) {
            nextSbd = CandidateCallBoard.resolveNextSbd(queue, callingSbd);
        }

        ExamRegistration callingCandidate = CandidateCallBoard.findBySbd(queue, callingSbd);
        ExamRegistration nextCandidate = CandidateCallBoard.findBySbd(queue, nextSbd);

        ExamSession currentSession = null;
        ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
        try {
            currentSession = sessionDAO.getById(sessionId);
        } catch (Exception ignored) {
        }

        Snapshot snapshot = new Snapshot();
        snapshot.sessionId = sessionId;
        snapshot.currentSession = currentSession;
        snapshot.callingCandidate = callingCandidate;
        snapshot.nextCandidate = nextCandidate;
        snapshot.callingActive = callingCandidate != null && !shiftEnded;
        snapshot.shiftEnded = shiftEnded;
        snapshot.updatedAtMs = updatedAtMs;
        return snapshot;
    }
}
