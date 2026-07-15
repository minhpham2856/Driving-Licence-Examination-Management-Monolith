package managingstaff.controller;

import auth.dto.UserDTO;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/manager/registrants")
public class RegistrantManagementServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/users.jsp";
    private static final int PAGE_SIZE = 15;
    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) return;

        String fullName = trim(request.getParameter("fullName"));
        String govIdNo = trim(request.getParameter("govIdNo"));
        String email = trim(request.getParameter("email"));
        String phoneNo = trim(request.getParameter("phoneNo"));
        String licence = trim(request.getParameter("licence")).toUpperCase(Locale.ROOT);
        String dossierStatus = trim(request.getParameter("dossierStatus"));
        String accountStatus = trim(request.getParameter("accountStatus"));
        int totalFiltered = dossierDAO.countRegistrants(
                dossierStatus, licence, fullName, govIdNo, email, phoneNo, accountStatus);
        int totalPages = Math.max(1, (totalFiltered + PAGE_SIZE - 1) / PAGE_SIZE);
        int currentPage = Math.min(Math.max(parseInt(request.getParameter("page")), 1), totalPages);
        List<DossierDTO> filtered = dossierDAO.findRegistrantPage(
                dossierStatus, licence, fullName, govIdNo, email, phoneNo,
                accountStatus, currentPage, PAGE_SIZE);

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
        DossierDTO target = dossierDAO.findByUserId(userId);
        if (target == null || target.getUser() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (dossierDAO.setUserActive(userId, active)) {
            AuditLogHelper.persistChange(request.getSession(), "UPDATE USER STATUS",
                    (active ? "Mở khóa" : "Khóa") + " tài khoản @" + target.getUser().getUsername(),
                    target.getUser().isActive() ? "Hoạt động" : "Đã khóa",
                    active ? "Hoạt động" : "Đã khóa", "User", userId);
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
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!SessionUtil.isManager(user)) {
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
