package controller.staff.managing;

import enums.ExamStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExamService;
import service.impl.ExamServiceImpl;
import dao.UserDAO;
import dao.impl.UserDAOImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/staff/managing/dashboard")
public class ManagingDashboardServlet extends HttpServlet {

    private final ExamService examService = new ExamServiceImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("totalStudents", userDAO.countAll());
        request.setAttribute("pendingApprovalsCount", 0);
        request.setAttribute("recentSubmissions", new ArrayList<Map<String, Object>>());

        // The upcoming-exams list was sourced from ExamViewDTO / SessionService
        // (getAllExams), both removed in the Session->Exam migration. ExamService
        // exposes only getById(int), so there is no in-scope list source here yet.
        // Left empty until a dashboard list method is provided by the lead.
        List<Map<String, Object>> upcomingExams = new ArrayList<>();
        request.setAttribute("upcomingExams", upcomingExams);
        request.getRequestDispatcher("/views/staff/managing/dashboard.jsp").forward(request, response);
    }
}
