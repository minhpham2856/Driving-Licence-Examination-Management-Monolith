package admin.controller;

import admin.dao.AdminStatsDAO;
import admin.dao.AuditLogViewDAO;
import admin.dao.impl.AdminStatsDAOImpl;
import admin.dao.impl.AuditLogViewDAOImpl;
import admin.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final AdminStatsDAO stats = new AdminStatsDAOImpl();
    private final AuditLogViewDAO auditDAO = new AuditLogViewDAOImpl();
    private static final String VIEW = "/views/admin/dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        req.setAttribute("zoneCount", stats.count("ExamZone"));
        req.setAttribute("areaCount", stats.count("ExamArea"));
        req.setAttribute("deviceCount", stats.count("ExamDevice"));
        req.setAttribute("licenceCount", stats.count("Licence"));
        req.setAttribute("feeCount", stats.count("Fee"));
        req.setAttribute("auditCount", stats.count("Audit"));
        req.setAttribute("accountCount", stats.countActiveAccounts());
        // 5 dòng nhật ký gần nhất
        req.setAttribute("recentLogs", auditDAO.search(null, null, null, null, null, 1, 5));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
