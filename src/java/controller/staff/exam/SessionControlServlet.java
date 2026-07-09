package controller.staff.exam;

import controller.staff.exam.adapter.StaffAuditLogSupport;
import controller.staff.exam.adapter.CallBoardHttpFacade;
import controller.staff.exam.module.ExamStaffWebModule;
import service.ExamSessionControlService;
import service.impl.ExamSessionControlServiceImpl;
import service.ExamStaffServices;
import util.SessionUserHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/session-control")
public class SessionControlServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamSessionControlService controlService = SERVICES.sessionControl();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int sessionId = parseSessionId(request, session);
        int staffId = SessionUserHelper.resolveUserId(session);
        String redirect = buildRedirect(request, sessionId);

        if ("startSession".equals(action)) {
            ExamSessionControlService.StartResult result = controlService.startSession(sessionId, staffId);
            if (result.isSuccess()) {
                applyRuntimeStart(getServletContext(), session, sessionId);
                auditLogSupport.persist(session, "UPDATE Session",
                        "Bắt đầu " + result.getSessionName()
                                + " (" + result.getExaminerCount() + " sát hạch viên)",
                        sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        } else if ("endSession".equals(action)) {
            ExamSessionControlService.EndResult result = controlService.endSession(sessionId);
            if (result.isSuccess()) {
                applyRuntimeEnd(getServletContext(), session, sessionId);
                auditLogSupport.persist(session, "UPDATE Session",
                        "Kết thúc " + result.getSessionName(), sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        }

        response.sendRedirect(redirect);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    private int parseSessionId(HttpServletRequest request, HttpSession session) {
        String param = request.getParameter("sessionId");
        if (param != null && !param.trim().isEmpty()) {
            try {
                return Integer.parseInt(param.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        Integer selected = (Integer) session.getAttribute("selectedSessionId");
        return selected != null ? selected : 2;
    }

    private String buildRedirect(HttpServletRequest request, int sessionId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/views/staff/examstaff/examiner-allocation?sessionId=" + sessionId;
        }
        return ctx + "/views/staff/examstaff/dashboard?sessionId=" + sessionId;
    }

    private void applyRuntimeStart(ServletContext ctx, HttpSession session, int sessionId) {
        if (ctx != null) {
            ctx.setAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID, sessionId);
        }
        if (session != null) {
            session.setAttribute("selectedSessionId", sessionId);
            session.removeAttribute("shiftEnded");
            session.removeAttribute("callingSbd");
        }
    }

    private void applyRuntimeEnd(ServletContext ctx, HttpSession session, int sessionId) {
        if (ctx != null) {
            Integer active = (Integer) ctx.getAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID);
            if (active != null && active == sessionId) {
                ctx.removeAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID);
            }
            callBoardHttp.sync(ctx, sessionId, null, null, true);
        }
        if (session != null) {
            Integer selected = (Integer) session.getAttribute("selectedSessionId");
            if (selected != null && selected == sessionId) {
                session.setAttribute("shiftEnded", "true");
                session.removeAttribute("callingSbd");
            }
        }
    }
}
