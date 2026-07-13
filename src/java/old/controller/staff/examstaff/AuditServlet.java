package controller.staff.examstaff;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Audit;
import model.User;
import service.AuditService;
import service.impl.AuditServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/staff/examstaff/audit")
public class AuditServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Resolve the current staff member from the session.
        int userId = 3; // Default staff (Trần Thị Thủ Tục, ID = 3) per branch.
        User user = (User) session.getAttribute("user");
        if (user != null && user.getUserId() > 0) {
            userId = user.getUserId();
        }

        // Optional single-day filter from the request (format yyyy-MM-dd).
        String filterDate = request.getParameter("filterDate");

        // Load the personal audit logs for this staff member via the service.
        List<Audit> personalLogs = auditService.getLogsByUser(userId, filterDate);
        if (personalLogs == null) {
            personalLogs = new ArrayList<>();
        }
        request.setAttribute("personalLogs", personalLogs);

        // Derived KPI counters computed from the loaded logs. The branch showed
        // "hồ sơ đã làm thủ tục" and "lệ phí" from a branch-only StaffProcedureKpi
        // model that does not exist in main, so we derive equivalent metrics from
        // the audit log itself instead of inventing that dependency.
        int procedureCount = 0;
        Set<String> entityTypes = new HashSet<>();
        for (Audit log : personalLogs) {
            String entity = log.getEntityName();
            if (entity != null && !entity.trim().isEmpty()) {
                entityTypes.add(entity);
                if ("Thanh toán".equals(entity) || "Hồ sơ".equals(entity)) {
                    procedureCount++;
                }
            }
        }
        request.setAttribute("procedureCount", procedureCount);
        request.setAttribute("entityTypeCount", entityTypes.size());

        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
