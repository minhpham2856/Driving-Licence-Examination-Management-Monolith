package Controllers.Staff.ManagingStaff;

import DAOs.AuditLogDAO;
import DAOs.DossierDAO;
import DAOs.ExamSessionDAO;
import DAOs.Impl.AuditLogDAOImpl;
import DAOs.Impl.DossierDAOImpl;
import DAOs.Impl.ExamSessionDAOImpl;
import DTOs.DossierDTO;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<DossierDTO> registrants = dossierDAO.findAllRegistrants();
        List<DossierDTO> reviewable = dossierDAO.findSubmitted();
        List<SessionDTO> activeSessions = sessionDAO.getActiveSessions();
        LocalDate today = LocalDate.now();

        long approved = registrants.stream().filter(d -> "Approved".equals(d.getStatus())).count();
        long locked = registrants.stream().filter(d -> !d.getUser().isActive()).count();
        long complete = registrants.stream().filter(DossierDTO::isComplete).count();
        long upcoming = activeSessions.stream()
                .filter(s -> s.getExamDate() != null
                        && !s.getExamDate().toLocalDate().isBefore(today))
                .count();

        Map<String, Long> licenceCounts = registrants.stream()
                .filter(d -> d.getLicenceClass() != null && !d.getLicenceClass().isBlank())
                .collect(Collectors.groupingBy(
                        DossierDTO::getLicenceClass,
                        LinkedHashMap::new,
                        Collectors.counting()));

        request.setAttribute("totalRegistrants", registrants.size());
        request.setAttribute("approvedCount", approved);
        request.setAttribute("reviewableCount", reviewable.size());
        request.setAttribute("lockedCount", locked);
        request.setAttribute("completeCount", complete);
        request.setAttribute("upcomingCount", upcoming);
        request.setAttribute("activeSessions", activeSessions.stream().limit(6).toList());
        request.setAttribute("recentDossiers", reviewable.stream().limit(6).toList());
        request.setAttribute("licenceCounts", licenceCounts);
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
