package service;

import enums.ExamSessionStatus;
import controller.staff.exam.CandidateCallBoard;
import controller.staff.exam.ExaminerSlot;
import dao.ExamSessionDAO;
import dao.ExaminerAssignmentDAO;
import dao.impl.ExamSessionDAOImpl;
import dao.impl.ExaminerAssignmentDAOImpl;
import model.exam.ExamSession;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public class ExamSessionControlService {

    public static final String CTX_ACTIVE_SESSION_ID = "examActiveSessionId";

    private final ExamSessionDAO sessiondao = new ExamSessionDAOImpl();
    private final ExaminerAssignmentDAO assignmentdao = new ExaminerAssignmentDAOImpl();

    public StartResult startSession(int sessionId, int staffUserId) {
        ExamSession examSession = sessiondao.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamSessionStatus.canStartSession(examSession.getStatus())) {
            if (ExamSessionStatus.isSessionInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlot> assignments = assignmentdao.getBySessionId(sessionId);
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công giám khảo vào khu vực thi. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi bắt đầu ca.");
        }

        if (!sessiondao.openSession(sessionId)) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }

        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }

    public EndResult endSession(int sessionId) {
        ExamSession examSession = sessiondao.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!ExamSessionStatus.isSessionInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessiondao.updateStatus(sessionId, ExamSessionStatus.COMPLETED.getStatus())) {
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
        return assignmentdao.getInProgressAssignmentsForExaminer(examinerUserId);
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
                            + examinerCount + " giám khảo có thể đăng nhập. "
                            + "Giờ ca thi đã được cập nhật cho thí sinh.",
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
                    "Đã kết thúc ca thi \"" + sessionName + "\". Giám khảo không thể đăng nhập ca này nữa.");
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
