package examstaff.controller;

import examstaff.util.AuditFilterSupport;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.service.impl.StaffAuditPageServiceImpl;
import examstaff.util.AllocationStageHelper;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.SessionUserHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/views/staff/examstaff/audit")
public class ExamStaffAuditServlet extends HttpServlet {

    private final StaffAuditPageServiceImpl auditPageService = new StaffAuditPageServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if ("true".equals(request.getParameter("exportExcel"))) {
            String filterDate = AuditFilterSupport.resolveFilterDate(request);
            String target = request.getContextPath() + "/views/staff/examstaff/audit-export";
            if (filterDate != null && !filterDate.isBlank()) {
                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);
            }
            response.sendRedirect(target);
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = AuditFilterSupport.resolveFilterDate(request);
        String filterKey = AuditFilterSupport.normalizeFilterKey(filterDate);

        Integer prevUserId = (Integer) session.getAttribute("auditPageUserId");
        String prevFilter = (String) session.getAttribute("auditPageFilterDate");
        boolean filterContextChanged = prevUserId == null || prevUserId != userId
                || prevFilter == null || !prevFilter.equals(filterKey);

        int page = AllocationStageHelper.parsePage(request.getParameter("page"));
        int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));

        StaffAuditPageViewDTO view = auditPageService.buildPage(
                userId, filterDate, page, pageSize, filterContextChanged);

        session.setAttribute("auditPageUserId", userId);
        session.setAttribute("auditPageFilterDate", view.getFilterKey());

        ExamStaffPageSupport.preparePage(request, false);

        bindAuditPage(request, view);
        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }

    private void bindAuditPage(HttpServletRequest request, StaffAuditPageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("personalLogs", view.getPersonalLogs());
        request.setAttribute("examStaffPageSlice", view.getPageSlice());
        request.setAttribute("examStaffListPath", "/views/staff/examstaff/audit");
        int completed = view.getProcedureKpi() != null ? view.getProcedureKpi().getCompletedCount() : 0;
        double totalFees = view.getProcedureKpi() != null ? view.getProcedureKpi().getTotalFees() : 0;
        request.setAttribute("myCompletedProcedures", completed);
        request.setAttribute("myTotalFees", totalFees);
    }
}
