package admin.controller;

import admin.constants.RoleUi;
import admin.dao.AuditLogViewDAO;
import admin.dao.impl.AuditLogViewDAOImpl;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin "Nháº­t kÃ½ há»‡ thá»‘ng" (audit log viewer).
 *   GET /admin/audit  -> list with filters: searchKeyword, filterRole, filterAction, dateFrom, dateTo, page
 * Read-only. IP/device is not stored in the Audit table, so that column shows "â€”".
 */
@WebServlet(name = "SystemLogServlet", urlPatterns = {"/admin/audit"})
public class SystemLogServlet extends HttpServlet {

    private final AuditLogViewDAO dao = new AuditLogViewDAOImpl();
    private static final int PAGE_SIZE = 15;
    private static final String LIST_VIEW = "/views/admin/audit.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String roleUi = Sanitize.text(req.getParameter("filterRole"));
        String action = Sanitize.text(req.getParameter("filterAction"));
        String dateFrom = Sanitize.text(req.getParameter("dateFrom"));
        String dateTo = Sanitize.text(req.getParameter("dateTo"));
        int page = Math.max(Sanitize.toInt(req.getParameter("page"), 1), 1);

        String dbRole = roleUi.isEmpty() ? null : RoleUi.toDbRole(roleUi);

        int total = dao.count(keyword, dbRole, action, dateFrom, dateTo);
        int totalPages = Math.max((int) Math.ceil(total / (double) PAGE_SIZE), 1);
        if (page > totalPages) page = totalPages;

        req.setAttribute("auditLogs", dao.search(keyword, dbRole, action, dateFrom, dateTo, page, PAGE_SIZE));

        // Stat cards
        int all = dao.countAll();
        int updateCount = dao.countByAction("UPDATE");
        int warningCount = dao.countByAction("WARNING");
        double successRate = (all == 0) ? 100.0 : ((all - warningCount) * 100.0 / all);
        req.setAttribute("totalActions", all);
        req.setAttribute("updateCount", updateCount);
        req.setAttribute("warningCount", warningCount);
        req.setAttribute("successRate", String.format("%.1f", successRate));

        // Pagination
        req.setAttribute("filteredTotal", total);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }
}
