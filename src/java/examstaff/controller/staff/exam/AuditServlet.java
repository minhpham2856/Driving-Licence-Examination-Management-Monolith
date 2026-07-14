package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.binder.StaffAuditPageBinder;
import examstaff.controller.staff.exam.http.AuditFilterSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.StaffAuditPageViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examstaff.service.StaffAuditPageService;
import examstaff.service.ExamStaffServices;
import examstaff.util.SessionUserHelper;
import examstaff.util.AllocationStageHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/examstaff/audit")
public class AuditServlet extends HttpServlet {

    private static final ExamStaffServices SERVICES = new ExamStaffWebModule().services();

    private final StaffAuditPageService auditPageService = SERVICES.auditPage();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if ("true".equals(request.getParameter("exportExcel"))) {
            String filterDate = AuditFilterSupport.resolveFilterDate(request);
            String target = request.getContextPath() + "/examstaff/audit-export";
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

        ExamStaffPageFacade.prepareExamStaffPage(request, session,
                request.getServletContext().getRealPath("/"));

        StaffAuditPageBinder.bind(request, view);
        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }
}
