package managingstaff.controller;

import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import auth.dto.UserDTO;
import managingstaff.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet("/manager/dossier-detail")
public class DossierDetailServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/user-detail.jsp";
    private static final int PAGE_SIZE = 15;
    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        int userId = parseInt(request.getParameter("id"));
        int registrationId = parseInt(request.getParameter("registrationId"));
        DossierDTO dossier = registrationId > 0
                ? dossierDAO.findByRegistrationId(registrationId)
                : userId > 0 ? dossierDAO.findByUserId(userId) : null;

        if ((userId > 0 || registrationId > 0) && dossier == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ học viên.");
            return;
        }

        if (userId <= 0 && registrationId <= 0) {
            String statusFilter = normalizeStatusFilter(request.getParameter("status"));
            int totalItems = dossierDAO.countRegistrants(
                    statusFilter, "", "", "", "", "", "");
            int totalPages = Math.max(1, (totalItems + PAGE_SIZE - 1) / PAGE_SIZE);
            int currentPage = Math.min(Math.max(parseInt(request.getParameter("page")), 1), totalPages);
            java.util.List<DossierDTO> dossiers = dossierDAO.findRegistrantPage(
                    statusFilter, "", "", "", "", "", "", currentPage, PAGE_SIZE);
            request.setAttribute("listMode", true);
            request.setAttribute("statusFilter", statusFilter);
            request.setAttribute("statusCounts", dossierDAO.countRegistrantStatuses());
            request.setAttribute("dossiers", dossiers);
            setPaginationAttributes(request, currentPage, totalPages, totalItems, dossiers.size());
        } else {
            request.setAttribute("dossier", dossier);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
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

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String normalizeStatusFilter(String value) {
        String filter = value == null ? "pending" : value.trim().toLowerCase(java.util.Locale.ROOT);
        return Set.of("all", "pending", "approved", "rejected", "present", "completed")
                .contains(filter) ? filter : "pending";
    }

    private static void setPaginationAttributes(HttpServletRequest request, int currentPage,
            int totalPages, int totalItems, int pageItems) {
        int firstItem = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalFiltered", totalItems);
        request.setAttribute("firstItem", firstItem);
        request.setAttribute("lastItem", totalItems == 0 ? 0 : firstItem + pageItems - 1);
        request.setAttribute("pageStart", Math.max(1, currentPage - 2));
        request.setAttribute("pageEnd", Math.min(totalPages, currentPage + 2));
    }
}
