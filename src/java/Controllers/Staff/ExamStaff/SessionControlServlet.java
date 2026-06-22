package Controllers.Staff.ExamStaff;

import Models.User;
import Services.ExamSessionControlService;
import Utils.AuditLogHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/session-control")
public class SessionControlServlet extends HttpServlet {

    private final ExamSessionControlService controlService = new ExamSessionControlService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int sessionId = parseSessionId(request, session);
        int staffId = resolveStaffId(session);
        String redirect = buildRedirect(request, sessionId);

        if ("startSession".equals(action)) {
            ExamSessionControlService.StartResult result = controlService.startSession(sessionId, staffId);
            if (result.isSuccess()) {
                controlService.applyRuntimeStart(getServletContext(), session, sessionId);
                AuditLogHelper.persist(session, "UPDATE Session",
                        "Bắt đầu ca thi SessionId=" + sessionId + " - " + result.getSessionName()
                                + " (" + result.getExaminerCount() + " sát hạch viên)",
                        sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        } else if ("endSession".equals(action)) {
            ExamSessionControlService.EndResult result = controlService.endSession(sessionId);
            if (result.isSuccess()) {
                controlService.applyRuntimeEnd(getServletContext(), session, sessionId);
                AuditLogHelper.persist(session, "UPDATE Session",
                        "Kết thúc ca thi SessionId=" + sessionId, sessionId);
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

    private int resolveStaffId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }

    private String buildRedirect(HttpServletRequest request, int sessionId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/views/staff/examstaff/examiner-allocation?sessionId=" + sessionId;
        }
        return ctx + "/views/staff/examstaff/dashboard.jsp?sessionId=" + sessionId;
    }
}
