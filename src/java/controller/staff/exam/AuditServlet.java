package controller.staff.exam;

import controller.staff.exam.support.StaffAuditPageBinder;
import dto.examstaff.StaffAuditPageViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.StaffAuditPageService;
import service.impl.StaffAuditPageServiceImpl;
import util.SessionUserHelper;
import util.examstaff.AllocationStageHelper;
import util.examstaff.AuditFilterHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/views/staff/examstaff/audit")
public class AuditServlet extends HttpServlet {

    private final StaffAuditPageService auditPageService = new StaffAuditPageServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if ("true".equals(request.getParameter("exportExcel"))) {
            String filterDate = AuditFilterHelper.resolveFilterDate(request);
            String target = request.getContextPath() + "/views/staff/examstaff/audit-export";
            if (filterDate != null && !filterDate.isBlank()) {
                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);
            }
            response.sendRedirect(target);
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = AuditFilterHelper.resolveFilterDate(request);
        String filterKey = AuditFilterHelper.normalizeFilterKey(filterDate);

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

        ExamStaffViewHelper.prepareExamStaffPage(request, session,
                request.getServletContext().getRealPath("/"));

        StaffAuditPageBinder.bind(request, view);
        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }
}
