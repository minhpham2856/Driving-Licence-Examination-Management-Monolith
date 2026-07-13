package controller.admin;
import dto.*;
import model.*;
import service.DashboardService;
import service.impl.DashboardServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private DashboardService dashboardService;
    @Override
    public void init() {
        dashboardService = new DashboardServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int totalAreas = dashboardService.getTotalExamCenters();
        req.setAttribute("totalExamCenters", totalAreas);
        req.setAttribute("totalUsers", dashboardService.getTotalUsers());
        req.setAttribute("totalExams", dashboardService.getTotalExams());
        req.setAttribute("totalComputers", dashboardService.getTotalComputers());
        req.setAttribute("auditLogs", dashboardService.getRecentActivities(8));
        req.getRequestDispatcher("/views/admin/dashboard.jsp").forward(req, resp);
    }
}
