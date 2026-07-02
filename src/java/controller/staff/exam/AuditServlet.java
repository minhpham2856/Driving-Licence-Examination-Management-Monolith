package controller.staff.exam;

import dao.AuditLogDAO;
import dao.impl.AuditLogDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import dto.staff.StaffProcedureKpiDTO;
import dto.user.AuditDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.SessionUserHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/audit")
public class AuditServlet extends HttpServlet {

    private final AuditLogDAO logDAO = new AuditLogDAOImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if ("true".equals(request.getParameter("exportExcel"))) {
            String filterDate = resolveFilterDate(request);
            String target = request.getContextPath() + "/views/staff/examstaff/audit-export";
            if (filterDate != null && !filterDate.isBlank()) {
                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);
            }
            response.sendRedirect(target);
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = resolveFilterDate(request);
        String filterKey = filterDate == null ? "" : filterDate.trim();

        Integer prevUserId = (Integer) session.getAttribute("auditPageUserId");
        String prevFilter = (String) session.getAttribute("auditPageFilterDate");
        boolean filterContextChanged = prevUserId == null || prevUserId != userId
                || prevFilter == null || !prevFilter.equals(filterKey);

        int page = AllocationStageHelper.parsePage(request.getParameter("page"));
        int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));
        if (filterContextChanged) {
            page = 1;
        }

        int totalLogs = logDAO.getLogsCountByUserAndDate(userId, filterDate);
        int totalPages = totalLogs <= 0 ? 0 : (int) Math.ceil((double) totalLogs / pageSize);
        if (totalPages > 0 && page > totalPages) {
            page = 1;
        } else if (totalPages == 0) {
            page = 1;
        }

        session.setAttribute("auditPageUserId", userId);
        session.setAttribute("auditPageFilterDate", filterKey);

        // apply vietnamese labels
        List<AuditDTO> personalLogs = loadLogs(userId, filterDate, page, pageSize);
        applyVietnameseLabels(personalLogs);
        StaffProcedureKpiDTO procedureKpi = logDAO.getStaffProcedureKpi(userId, filterDate);

        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffViewHelper.prepareExamStaffPage(request, session, new ExamSessionDAOImpl(), webRoot);

        request.setAttribute("personalLogs", personalLogs);
        request.setAttribute("examStaffPageSlice",
                new AllocationStageHelper.PageSlice<>(personalLogs, page, pageSize, totalLogs));
        request.setAttribute("examStaffListPath", "/views/staff/examstaff/audit");
        request.setAttribute("myCompletedProcedures", procedureKpi.getCompletedCount());
        request.setAttribute("myTotalFees", procedureKpi.getTotalFees());

        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    // Xac dinh filter date
    }

    private static String resolveFilterDate(HttpServletRequest request) {
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.isBlank()) {
            filterDate = request.getParameter("date");
        }
    // Tai logs
        return filterDate;
    }

    private List<AuditDTO> loadLogs(int userId, String filterDate, int page, int pageSize) {
        try {
            return logDAO.getLogsByUserAndDatePaginated(userId, filterDate, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
    // apply vietnamese labels
            return new ArrayList<>();
        }
    }

    private static void applyVietnameseLabels(List<AuditDTO> logs) {
        if (logs == null) {
            return;
        }
        for (AuditDTO log : logs) {
            AuditExportLabels.applyDisplayLabels(log);
        }
    }
}
