package controller.staff.exam;

import dto.examstaff.StaffAuditPageViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.StaffAuditPageService;
import service.impl.StaffAuditPageServiceImpl;
import util.SessionUserUtil;
import util.examstaff.AllocationStageUtil;
import util.examstaff.AuditFilterUtil;

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
            String filterDate = AuditFilterUtil.resolveFilterDate(request);
            String target = request.getContextPath() + "/views/staff/examstaff/audit-export";
            if (filterDate != null && !filterDate.isBlank()) {
                target += "?filterDate=" + URLEncoder.encode(filterDate, StandardCharsets.UTF_8);
            }
            response.sendRedirect(target);
            return;
        }

        int userId = SessionUserUtil.resolveUserId(session);
        String filterDate = AuditFilterUtil.resolveFilterDate(request);
        String filterKey = AuditFilterUtil.normalizeFilterKey(filterDate);

        Integer prevUserId = (Integer) session.getAttribute("auditPageUserId");
        String prevFilter = (String) session.getAttribute("auditPageFilterDate");
        boolean filterContextChanged = prevUserId == null || prevUserId != userId
                || prevFilter == null || !prevFilter.equals(filterKey);

        int page = AllocationStageUtil.parsePage(request.getParameter("page"));
        int pageSize = AllocationStageUtil.parsePageSize(request.getParameter("size"));

        StaffAuditPageViewDTO view = auditPageService.buildPage(
                userId, filterDate, page, pageSize, filterContextChanged);

        session.setAttribute("auditPageUserId", userId);
        session.setAttribute("auditPageFilterDate", view.getFilterKey());

        BaseExamStaffServlet.prepareExamStaffPage(request, session,
                request.getServletContext().getRealPath("/"));

        BaseExamStaffServlet.bind(request, view);
        request.getRequestDispatcher("/views/staff/examstaff/audit.jsp").forward(request, response);
    }
}
