package controller.admin;


import service.AdminDashboardService;
import service.impl.AdminDashboardServiceImpl;

import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private AdminDashboardService dashboardService;

    @Override
    public void init() {
        dashboardService = new AdminDashboardServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        int totalAreas = dashboardService.getTotalExamCenters();
        req.setAttribute("totalExamCenters", totalAreas);
        req.setAttribute("totalUsers", dashboardService.getTotalUsers());
        req.setAttribute("totalExamSessions", dashboardService.getTotalExamSessions());
        req.setAttribute("totalComputers", dashboardService.getTotalComputers());
        req.setAttribute("auditLogs", dashboardService.getRecentActivities(8));

        req.getRequestDispatcher("/views/admin/dashboard.jsp").forward(req, resp);
    }
}

