package controller.staff.examstaff;

import dto.ServiceResult;
import dto.SessionStartDTO;
import dto.SessionViewDTO;
import enums.AuditAction;
import enums.AuditEntity;
import enums.ExamSessionStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.AuditService;
import service.SessionService;
import service.impl.AuditServiceImpl;
import service.impl.SessionServiceImpl;

import java.io.IOException;

@WebServlet({"/staff/examstaff/session-control", "/views/staff/examstaff/dashboard"})
public class SessionControlServlet extends HttpServlet {

    // Service layer only; controllers must never touch DAOs or DB connections.
    private final SessionService sessionService = new SessionServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int sessionId = parseSessionId(request, session);
        int staffId = resolveStaffId(session);
        String redirect = buildRedirect(request, sessionId);

        if ("startSession".equals(action)) {
            // Start the exam session via the service.
            ServiceResult<SessionStartDTO> result = sessionService.startSession(sessionId, staffId);
            if (result.isSuccess()) {
                SessionStartDTO data = result.getData();
                int examinerCount = data != null ? data.getExaminerCount() : 0;
                String sessionLabel = (data != null && data.getSessionLabel() != null)
                        ? data.getSessionLabel() : "";
                String message = "Bắt đầu ca thi SessionId=" + sessionId + " — " + sessionLabel
                        + " (" + examinerCount + " giám khảo)";
                // Record the action in the audit log.
                auditService.logAction(staffId, AuditAction.UPDATE, AuditEntity.EXAM_SESSION, message, sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        } else if ("endSession".equals(action)) {
            // End the exam session via the service.
            ServiceResult<SessionStartDTO> result = sessionService.endSession(sessionId);
            if (result.isSuccess()) {
                String message = "Kết thúc ca thi SessionId=" + sessionId;
                auditService.logAction(staffId, AuditAction.UPDATE, AuditEntity.EXAM_SESSION, message, sessionId);
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
        HttpSession session = request.getSession();
        int sessionId = parseSessionId(request, session);

        // Keep the selected session in sync for the sidebar and header.
        session.setAttribute("selectedSessionId", sessionId);
        request.setAttribute("selectedSessionId", sessionId);

        // Load the current session and the full list via the service layer.
        SessionViewDTO currentSession = sessionService.getSessionById(sessionId);
        request.setAttribute("currentSession", currentSession);

        // Normalize the status into an enum name for the view branching.
        String statusName = "UNKNOWN";
        String statusValue = "Không xác định";
        if (currentSession != null && currentSession.getStatus() != null) {
            ExamSessionStatus status = ExamSessionStatus.fromValue(currentSession.getStatus());
            if (status != null) {
                statusName = status.name();
                statusValue = status.getValue();
            }
        }
        request.setAttribute("sessionStatusName", statusName);
        request.setAttribute("sessionStatusValue", statusValue);

        request.setAttribute("allSessions", sessionService.getAllSessions());
        request.setAttribute("assignedExaminerCount", sessionService.getAssignedExaminerCount(sessionId));

        // Move one-shot flash messages from the session into the request.
        String msg = (String) session.getAttribute("sessionControlMsg");
        if (msg != null) {
            request.setAttribute("sessionControlMsg", msg);
            session.removeAttribute("sessionControlMsg");
        }
        String error = (String) session.getAttribute("sessionControlError");
        if (error != null) {
            request.setAttribute("sessionControlError", error);
            session.removeAttribute("sessionControlError");
        }

        request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
    }

    private int parseSessionId(HttpServletRequest request, HttpSession session) {
        String param = request.getParameter("sessionId");
        if (param != null && !param.trim().isEmpty()) {
            try {
                return Integer.parseInt(param.trim());
            } catch (NumberFormatException ignored) {
                // Fall through to the session/default value.
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
        return ctx + "/views/staff/examstaff/dashboard?sessionId=" + sessionId;
    }
}
