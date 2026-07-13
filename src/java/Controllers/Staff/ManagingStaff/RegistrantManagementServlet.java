package Controllers.Staff.ManagingStaff;

import DAOs.DossierDAO;
import DAOs.UserDAO;
import DAOs.Impl.DossierDAOImpl;
import DAOs.Impl.UserDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/manager/registrants")
public class RegistrantManagementServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/users.jsp";
    private static final int PAGE_SIZE = 15;
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) return;

        String keyword = trim(request.getParameter("keyword"));
        String licence = trim(request.getParameter("licence")).toUpperCase(Locale.ROOT);
        String dossierStatus = trim(request.getParameter("dossierStatus"));
        String accountStatus = trim(request.getParameter("accountStatus"));
        int totalFiltered = dossierDAO.countRegistrants(
                dossierStatus, licence, keyword, accountStatus);
        int totalPages = Math.max(1, (totalFiltered + PAGE_SIZE - 1) / PAGE_SIZE);
        int currentPage = Math.min(Math.max(parseInt(request.getParameter("page")), 1), totalPages);
        List<DossierDTO> filtered = dossierDAO.findRegistrantPage(
                dossierStatus, licence, keyword, accountStatus, currentPage, PAGE_SIZE);

        Map<String, Integer> statusCounts = dossierDAO.countRegistrantStatuses();
        Map<String, Integer> approvedByLicence = dossierDAO.countApprovedByLicence();
        request.setAttribute("registrantReady", true);
        request.setAttribute("registrants", filtered);
        setPaginationAttributes(request, currentPage, totalPages, totalFiltered, filtered.size());
        request.setAttribute("totalRegistrants", statusCounts.getOrDefault("all", 0));
        request.setAttribute("approvedCount", statusCounts.getOrDefault("approved", 0));
        request.setAttribute("pendingCount",
                statusCounts.getOrDefault("pending", 0)
                + statusCounts.getOrDefault("supplement", 0)
                + statusCounts.getOrDefault("rejected", 0));
        request.setAttribute("lockedCount", dossierDAO.countLockedRegistrants());
        request.setAttribute("approvedByLicence", approvedByLicence);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!hasAccess(request, response)) return;
        int userId = parseInt(request.getParameter("id"));
        boolean active = "activate".equals(request.getParameter("action"));
        User target = userDAO.getById(userId);
        if (target == null || target.getRole() == null
                || !"Registrant".equalsIgnoreCase(target.getRole().getRoleName())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (userDAO.updateStatus(userId, active)) {
            AuditLogHelper.persistChange(request.getSession(), "UPDATE USER STATUS",
                    (active ? "Mở khóa" : "Khóa") + " tài khoản @" + target.getUsername(),
                    target.isActive() ? "Hoạt động" : "Đã khóa",
                    active ? "Hoạt động" : "Đã khóa", null, userId);
            request.getSession().setAttribute("registrantSuccess",
                    active ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } else {
            request.getSession().setAttribute("registrantError",
                    "Không thể cập nhật trạng thái tài khoản.");
        }
        response.sendRedirect(request.getContextPath() + "/manager/registrants");
    }

    private static void setPaginationAttributes(HttpServletRequest request, int currentPage,
            int totalPages, int totalItems, int pageItems) {
        int firstItem = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        int lastItem = totalItems == 0 ? 0 : firstItem + pageItems - 1;
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalFiltered", totalItems);
        request.setAttribute("firstItem", firstItem);
        request.setAttribute("lastItem", lastItem);
        request.setAttribute("pageStart", Math.max(1, currentPage - 2));
        request.setAttribute("pageEnd", Math.min(totalPages, currentPage + 2));
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception ignored) { return 0; }
    }
}
