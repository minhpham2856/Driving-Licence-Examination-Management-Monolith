// Forced recompilation trigger
package service.impl;

import controller.staff.exam.CandidateCallBoard;
import service.ExamSessionControlService;

import dto.ExaminerSlotDTO;
import dao.ExamSessionDAO;
import dao.ExaminerAssignmentDAO;
import dao.impl.ExamSessionDAOImpl;
import dao.impl.ExaminerAssignmentDAOImpl;
import dto.SessionDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public class ExamSessionControlServiceImpl implements ExamSessionControlService {

    public static final String CTX_ACTIVE_SESSION_ID = "examActiveSessionId";

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();

    @Override
    public StartResult startSession(int sessionId, int staffUserId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return StartResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enums.ExamSessionStatus.canStart(examSession.getStatus())) {
            if (enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
                return StartResult.fail("Ca thi \"" + examSession.getSessionName() + "\" đã được bắt đầu.");
            }
            return StartResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" không thể bắt đầu (trạng thái: " + examSession.getStatus() + ").");
        }

        List<ExaminerSlotDTO> assignments = assignmentDAO.getBySessionId(sessionId);
        long withArea = assignments.stream().filter(s -> s.getAreaId() > 0).count();
        if (withArea == 0) {
            return StartResult.fail("Chưa phân công sát hạch viên vào khu vực thi. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu ca.");
        }

        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.DANG_DIEN_RA.getDisplayName())) {
            return StartResult.fail("Không cập nhật được trạng thái ca thi trên cơ sở dữ liệu.");
        }

        return StartResult.ok(examSession.getSessionName(), (int) withArea);
    }

    @Override
    public EndResult endSession(int sessionId) {
        SessionDTO examSession = sessionDAO.getById(sessionId);
        if (examSession == null) {
            return EndResult.fail("Không tìm thấy ca thi (SessionId=" + sessionId + ").");
        }
        if (!enums.ExamSessionStatus.isInProgress(examSession.getStatus())) {
            return EndResult.fail("Ca thi \"" + examSession.getSessionName()
                    + "\" chưa ở trạng thái đang diễn ra (hiện tại: " + examSession.getStatus() + ").");
        }
        if (!sessionDAO.updateStatus(sessionId, enums.ExamSessionStatus.HOAN_TAT.getDisplayName())) {
            return EndResult.fail("Không cập nhật được trạng thái kết thúc ca thi.");
        }
        return EndResult.ok(examSession.getSessionName());
    }

    @Override
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

    @Override
    public void applyRuntimeEnd(ServletContext ctx, HttpSession httpSession, int sessionId) {
        if (ctx != null) {
            Integer active = (Integer) ctx.getAttribute(CTX_ACTIVE_SESSION_ID);
            if (active != null && active == sessionId) {
                ctx.removeAttribute(CTX_ACTIVE_SESSION_ID);
            }
            CandidateCallBoard.sync(ctx, sessionId, null, null, true);
        }
        if (httpSession != null) {
            Integer selected = (Integer) httpSession.getAttribute("selectedSessionId");
            if (selected != null && selected == sessionId) {
                httpSession.setAttribute("shiftEnded", "true");
                httpSession.removeAttribute("callingSbd");
            }
        }
    }

    @Override
    public List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId) {
        return assignmentDAO.getInProgressAssignmentsForExaminer(examinerUserId);
    }
}






