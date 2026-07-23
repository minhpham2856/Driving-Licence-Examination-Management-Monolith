package policestaff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.service.PoliceDashboardService;
import policestaff.service.impl.PoliceDashboardServiceImpl;

@WebServlet("/police/dashboard")
public class PoliceDashboardServlet extends HttpServlet {
    private final PoliceDashboardService dashboardService = new PoliceDashboardServiceImpl();
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int page = positive(request.getParameter("page"), 1);
            int total = dashboardService.countSubmissions(null, null);
            int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(page, totalPages);
            List<PoliceSubmissionDTO> submissions =
                    dashboardService.loadSubmissions(null, null, page, PAGE_SIZE);
            request.setAttribute("submissions", submissions);
            request.setAttribute("pendingSubmissionCount", dashboardService.countSubmissions("PENDING", null));
            request.setAttribute("completedSubmissionCount", dashboardService.countSubmissions("COMPLETED", null));
            request.setAttribute("pendingCandidateCount", dashboardService.countPendingCandidates());
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalSubmissions", total);
        } catch (RuntimeException ex) {
            request.setAttribute("policeDashboardError", ex.getMessage());
            request.setAttribute("submissions", List.of());
        }
        request.getRequestDispatcher("/views/staff/policestaff/dashboard.jsp").forward(request, response);
    }

    private static int positive(String value, int fallback) {
        try { return Math.max(1, Integer.parseInt(value)); } catch (Exception ex) { return fallback; }
    }
}
