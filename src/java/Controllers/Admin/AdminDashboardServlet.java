package Controllers.Admin;

import DAO.AdminStatsDAO;
import DAO.ExamRoomDAO;
import DAO.Impl.ExamRoomDAOImpl;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final AdminStatsDAO stats = new AdminStatsDAO();
    private final ExamRoomDAO roomDAO = new ExamRoomDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        int totalAreas = stats.countExamAreas();
        req.setAttribute("totalExamCenters", totalAreas);
        req.setAttribute("totalExamRooms", roomDAO.countAll());
        req.setAttribute("totalUsers", stats.countUsers());
        req.setAttribute("totalExamSessions", stats.countExams());
        req.setAttribute("totalComputers", stats.countDevices());
        req.setAttribute("auditLogs", stats.recentActivity(8));

        req.getRequestDispatcher("/views/admin/dashboard.jsp").forward(req, resp);
    }
}

