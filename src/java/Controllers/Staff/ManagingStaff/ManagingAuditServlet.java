package Controllers.Staff.ManagingStaff;

import DAOs.AuditLogDAO;
import DAOs.Impl.AuditLogDAOImpl;
import DTOs.AuditDTO;
import Models.User;
import Utils.Sanitize;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/manager/audit")
public class ManagingAuditServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/audit.jsp";
    private static final int PAGE_SIZE = 15;
    private static final Set<String> ACTIONS = Set.of(
            "APPROVE", "INSERT", "UPDATE", "DELETE", "EXPORT",
            "ASSIGN", "IMPORT", "WARNING", "SYSTEM");
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = requireManager(request, response);
        if (currentUser == null) return;

        String keyword = Sanitize.text(request.getParameter("keyword"));
        String action = normalizeAction(request.getParameter("action"));
        String startDate = validDate(request.getParameter("startDate"));
        String endDate = validDate(request.getParameter("endDate"));
        if (hasText(request.getParameter("startDate")) && startDate.isEmpty()
                || hasText(request.getParameter("endDate")) && endDate.isEmpty()) {
            request.setAttribute("auditFilterError", "Ngày lọc không hợp lệ nên đã được bỏ qua.");
        }
        if (!startDate.isEmpty() && !endDate.isEmpty()
                && LocalDate.parse(startDate).isAfter(LocalDate.parse(endDate))) {
            String swap = startDate;
            startDate = endDate;
            endDate = swap;
            request.setAttribute("auditFilterError", "Khoảng ngày đã được sắp xếp lại từ ngày sớm đến ngày muộn.");
        }

        int total = auditDAO.countUserLogs(
                currentUser.getId(), keyword, action, startDate, endDate);
        int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int currentPage = Math.min(Math.max(
                Sanitize.toInt(request.getParameter("page"), 1), 1), totalPages);
        List<AuditDTO> logs = auditDAO.searchUserLogsPaginated(
                currentUser.getId(), keyword, action, startDate, endDate,
                currentPage, PAGE_SIZE);

        request.setAttribute("auditReady", true);
        request.setAttribute("logs", logs);
        request.setAttribute("keyword", keyword);
        request.setAttribute("action", action);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("totalOperations", total);
        request.setAttribute("approvedCount",
                auditDAO.countUserLogs(currentUser.getId(), "Approved hồ sơ", "", startDate, endDate)
                + auditDAO.countUserLogs(currentUser.getId(), "", "APPROVE", startDate, endDate));
        request.setAttribute("supplementCount",
                auditDAO.countUserLogs(currentUser.getId(), "NeedSupplement hồ sơ", "", startDate, endDate));
        request.setAttribute("exportCount",
                auditDAO.countUserLogs(currentUser.getId(), "", "EXPORT", startDate, endDate));
        setPagination(request, currentPage, totalPages, total, logs.size());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static void setPagination(HttpServletRequest request, int page,
            int totalPages, int total, int pageItems) {
        int first = total == 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalFiltered", total);
        request.setAttribute("firstItem", first);
        request.setAttribute("lastItem", total == 0 ? 0 : first + pageItems - 1);
        request.setAttribute("pageStart", Math.max(1, page - 2));
        request.setAttribute("pageEnd", Math.min(totalPages, page + 2));
    }

    private static String normalizeAction(String value) {
        String action = Sanitize.text(value).toUpperCase(Locale.ROOT);
        return ACTIONS.contains(action) ? action : "";
    }

    private static String validDate(String value) {
        String date = Sanitize.text(value);
        if (date.isEmpty()) return "";
        try {
            return LocalDate.parse(date).toString();
        } catch (DateTimeParseException ignored) {
            return "";
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
