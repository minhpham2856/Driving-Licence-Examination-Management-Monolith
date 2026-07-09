package controller.staff.managing;

import dto.SessionViewDTO;
import enums.ExamSessionStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SessionService;
import service.impl.SessionServiceImpl;
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

    private final SessionService sessionService = new SessionServiceImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("totalStudents", userDAO.countAll());
        request.setAttribute("pendingApprovalsCount", 0);
        request.setAttribute("recentSubmissions", new ArrayList<Map<String, Object>>());

        List<Map<String, Object>> upcomingExams = new ArrayList<>();
        for (SessionViewDTO session : sessionService.getAllSessions()) {
            ExamSessionStatus status = ExamSessionStatus.fromValue(session.getStatus());
            if (status == ExamSessionStatus.COMPLETED || status == ExamSessionStatus.CANCELLED) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("shiftLabel", session.getSessionLabel());
            row.put("licenseClass", session.getLicenseCode() != null ? session.getLicenseCode() : "-");
            if (session.getExamDate() != null) {
                synchronized (DATE_FMT) {
                    row.put("examDate", DATE_FMT.format(session.getExamDate()));
                }
            } else {
                row.put("examDate", "-");
            }
            row.put("registeredCount", session.getRegisteredCount());
            row.put("status", session.getStatus());
            if (status == ExamSessionStatus.IN_PROGRESS) {
                row.put("statusClass", "success");
            } else if (status == ExamSessionStatus.NOT_STARTED) {
                row.put("statusClass", "info");
            } else {
                row.put("statusClass", "warning");
            }
            upcomingExams.add(row);
            if (upcomingExams.size() >= 5) {
                break;
            }
        }
        request.setAttribute("upcomingExams", upcomingExams);
        request.getRequestDispatcher("/views/staff/managing/dashboard.jsp").forward(request, response);
    }
}
