package Services;

import Utils.ExamConstants;
import Controllers.Staff.ExamStaff.CandidateCallBoard;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAOs.ExamSessionDAO;
import DAOs.ExaminerAssignmentDAO;
import DAOs.Impl.ExamSessionDAOImpl;
import DAOs.Impl.ExaminerAssignmentDAOImpl;
import DTOs.SessionDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public class ExamSessionControlService {

    public static final String CTX_ACTIVE_SESSION_ID = "examActiveSessionId";

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();

    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamConstants.canStartSession(examSession.getStatus())) {
            if (ExamConstants.isSessionInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlot> assignments = assignmentDAO.getBySessionId(sessionId);
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }

        if (!sessionDAO.updateStatus(sessionId, ExamConstants.SESSION_IN_PROGRESS)) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }

        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }

    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamConstants.isSessionInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, ExamConstants.SESSION_COMPLETED)) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc ca thi.");
        }
        return EndResult.ok(examSession.getSessionName());
    }

    public void applyRuntimeStart(ServletContext ctx, HttpSession httpSession, int sessionId) {
        if (ctx != null) {
            ctx.setAttribute(CTX_ACTIVE_SESSION_ID, sessionId);
        }
        if (httpSession != null) {
            httpSession.setAttribute("selectedSessionId", sessionId);
            httpSession.removeAttribute("shiftEnded");
            httpSession.removeAttribute("callingSbd");
        }
    }

    public void applyRuntimeEnd(ServletContext ctx, HttpSession httpSession, int sessionId) {
        if (ctx != null) {
            Integer active = (Integer) ctx.getAttribute(CTX_ACTIVE_SESSION_ID);
            if (active != null && active == sessionId) {
                ctx.removeAttribute(CTX_ACTIVE_SESSION_ID);
            }
            CandidateCallBoard.State board = CandidateCallBoard.getState(ctx, sessionId);
            if (board != null) {
                board.setShiftEnded(true);
                board.setCallingSbd(null);
                board.setUpdatedAtMs(System.currentTimeMillis());
            }
        }
        if (httpSession != null) {
            Integer selected = (Integer) httpSession.getAttribute("selectedSessionId");
            if (selected != null && selected == sessionId) {
                httpSession.setAttribute("shiftEnded", "true");
                httpSession.removeAttribute("callingSbd");
            }
        }
    }

    public List<ExaminerSlot> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }

    public static final class StartResult {
        private final boolean success;
        private final String message;
        private final String sessionName;
        private final int examinerCount;

        private StartResult(boolean success, String message, String sessionName, int examinerCount) {
            this.success = success;
            this.message = message;
            this.sessionName = sessionName;
            this.examinerCount = examinerCount;
        }

        public static StartResult ok(String sessionName, int examinerCount) {
            return new StartResult(true,
                    "Đã bắt đầu ca thi \"" + sessionName + "\". "
                            + examinerCount + " sát hạch viên có thể đăng nhập.",
                    sessionName, examinerCount);
        }

        public static StartResult fail(String message) {
            return new StartResult(false, message, null, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSessionName() {
            return sessionName;
        }

        public int getExaminerCount() {
            return examinerCount;
        }
    }

    public static final class EndResult {
        private final boolean success;
        private final String message;

        private EndResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static EndResult ok(String sessionName) {
            return new EndResult(true,
                    "Đã kết thúc ca thi \"" + sessionName + "\". sát hạch viên không thể đăng nhập ca này nữa.");
        }

        public static EndResult fail(String message) {
            return new EndResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
