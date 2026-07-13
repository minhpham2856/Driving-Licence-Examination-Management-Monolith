package Controllers.Staff.ManagingStaff;

import DAOs.ExamSessionDAO;
import DAOs.Impl.ExamSessionDAOImpl;
import DTOs.SessionDTO;
import Models.User;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebServlet("/manager/exam-schedules")
public class ExamScheduleServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/exam-schedules.jsp";
    private static final Set<String> ALLOWED_STATUS = Set.of("Scheduled", "Open", "Closed", "Cancelled");

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }
        bindPageData(request);
        moveFlash(request, "scheduleSuccess");
        moveFlash(request, "scheduleError");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("status".equalsIgnoreCase(action)) {
            updateStatus(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules/create");
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int sessionId = parseInt(request.getParameter("sessionId"), 0);
        String status = trim(request.getParameter("status"));
        if (sessionId <= 0 || !ALLOWED_STATUS.contains(status)) {
            request.getSession().setAttribute("scheduleError", "Trạng thái phiên thi không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
            return;
        }

        boolean ok = sessionDAO.updateStatus(sessionId, status);
        request.getSession().setAttribute(ok ? "scheduleSuccess" : "scheduleError",
                ok ? "Đã cập nhật trạng thái phiên thi." : "Không cập nhật được trạng thái phiên thi.");
        if (ok) {
            AuditLogHelper.persist(request.getSession(), "UPDATE SESSION",
                    "Cập nhật trạng thái phiên thi thành " + status, sessionId);
        }
        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules");
    }

    private void bindPageData(HttpServletRequest request) {
        List<SessionDTO> sessions = sessionDAO.getAllSessions();
        request.setAttribute("sessions", sessions);
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!"ManagingStaff".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void moveFlash(HttpServletRequest request, String name) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute(name);
        if (value != null) {
            request.setAttribute(name, value);
            session.removeAttribute(name);
        }
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(trim(raw));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
