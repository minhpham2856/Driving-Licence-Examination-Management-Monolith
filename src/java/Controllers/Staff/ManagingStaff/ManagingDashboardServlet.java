package Controllers.Staff.ManagingStaff;

import DAOs.AuditLogDAO;
import DAOs.DossierDAO;
import DAOs.ExamSessionDAO;
import DAOs.Impl.AuditLogDAOImpl;
import DAOs.Impl.DossierDAOImpl;
import DAOs.Impl.ExamSessionDAOImpl;
import DTOs.SessionDTO;
import Models.User;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/manager/dashboard")
public class ManagingDashboardServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/dashboard.jsp";
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = requireManager(request, response);
        if (currentUser == null) return;

        Map<String, Integer> statusCounts = dossierDAO.countRegistrantStatuses();
        int reviewableCount = dossierDAO.countSubmitted();
        List<SessionDTO> activeSessions = sessionDAO.getActiveSessions();
        LocalDate today = LocalDate.now();

        long upcoming = activeSessions.stream()
                .filter(s -> s.getExamDate() != null
                        && !s.getExamDate().toLocalDate().isBefore(today))
                .count();

        request.setAttribute("totalRegistrants", statusCounts.getOrDefault("all", 0));
        request.setAttribute("approvedCount", statusCounts.getOrDefault("approved", 0));
        request.setAttribute("reviewableCount", reviewableCount);
        request.setAttribute("lockedCount", dossierDAO.countLockedRegistrants());
        request.setAttribute("completeCount", dossierDAO.countCompleteRegistrants());
        request.setAttribute("upcomingCount", upcoming);
        request.setAttribute("activeSessions", activeSessions.stream().limit(6).toList());
        request.setAttribute("recentDossiers", dossierDAO.findSubmittedPage(1, 6));
        request.setAttribute("licenceCounts", dossierDAO.countRegistrantsByLicence());
        request.setAttribute("recentAudits",
                auditDAO.getLogsByUserAndDate(currentUser.getId(), null).stream().limit(6).toList());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private User requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }
}
