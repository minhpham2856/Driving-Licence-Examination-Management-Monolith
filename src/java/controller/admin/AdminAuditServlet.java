package controller.admin;

import service.AuditService;
import service.impl.AuditServiceImpl;
import util.FormatUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminAuditServlet", urlPatterns = {"/admin/audit"})
public class AdminAuditServlet extends HttpServlet {

    private AuditService auditService;

    @Override
    public void init() {
        auditService = new AuditServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = FormatUtil.text(req.getParameter("searchKeyword"));
        List<Map<String, Object>> logs = auditService.searchLogs(keyword, 100);
        req.setAttribute("logs", logs);
        req.getRequestDispatcher("/views/admin/audit.jsp").forward(req, resp);
    }
}
