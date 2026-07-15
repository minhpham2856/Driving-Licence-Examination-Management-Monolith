package examstaff.controller;

import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.service.AuditService;
import examstaff.service.impl.AuditServiceImpl;
import examstaff.service.impl.support.allocation.AllocationStageHelper;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Trang nhật ký audit cá nhân của staff: lọc ngày + paging + KPI thủ tục; export chuyển sang audit-export.
 */
@WebServlet("/examstaff/audit")
public class AuditServlet extends HttpServlet {

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: (exportExcel → redirect audit-export) hoặc buildPage → prepare sidebar → bind → JSP.
     *
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi redirect
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Nhánh xuất Excel → servlet stream riêng
        if ("true".equals(request.getParameter("exportExcel"))) {
            String filterDate = resolveFilterDate(request);
            String target = request.getContextPath() + "/examstaff/audit-export";
            if (filterDate != null && !filterDate.isBlank()) {
                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);
            }
            response.sendRedirect(target);
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = resolveFilterDate(request);
        String filterKey = examstaff.util.AuditFilterHelper.normalizeFilterKey(filterDate);

        // Đổi user/ngày lọc → reset paging về trang đầu (service nhận filterContextChanged)
        Integer prevUserId = (Integer) session.getAttribute("auditPageUserId");
        String prevFilter = (String) session.getAttribute("auditPageFilterDate");
        boolean filterContextChanged = prevUserId == null || prevUserId != userId
                || prevFilter == null || !prevFilter.equals(filterKey);

        int page = AllocationStageHelper.parsePage(request.getParameter("page"));
        int pageSize = AllocationStageHelper.parsePageSize(request.getParameter("size"));

        StaffAuditPageViewDTO view = auditService.buildPage(
                userId, filterDate, page, pageSize, filterContextChanged);

        session.setAttribute("auditPageUserId", userId);
        session.setAttribute("auditPageFilterDate", view.getFilterKey());

        ExamStaffPageSupport.prepareExamStaffPage(request, session,
                request.getServletContext().getRealPath("/"), true, viewService);

        bindAuditPage(request, view);
        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }

    /**
     * Đọc {@code filterDate} (ưu tiên) hoặc {@code date} từ request.
     *
     * @return chuỗi ngày lọc hoặc null
     */
    private static String resolveFilterDate(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.isBlank()) {
            filterDate = request.getParameter("date");
        }
        return filterDate;
    }

    /**
     * Bind personalLogs / paging / KPI lên request cho JSP audit.
     *
     * @param view DTO trang audit đã build
     */
    private static void bindAuditPage(HttpServletRequest request, StaffAuditPageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("personalLogs", view.getPersonalLogs());
        request.setAttribute("examStaffPageSlice", view.getPageSlice());
        request.setAttribute("examStaffListPath", "/examstaff/audit");
        int completed = view.getProcedureKpi() != null ? view.getProcedureKpi().getCompletedCount() : 0;
        double totalFees = view.getProcedureKpi() != null ? view.getProcedureKpi().getTotalFees() : 0;
        request.setAttribute("myCompletedProcedures", completed);
        request.setAttribute("myTotalFees", totalFees);
    }
}
