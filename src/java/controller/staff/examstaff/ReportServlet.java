package controller.staff.examstaff;

import dto.ExamReportDTO;
import dto.SessionViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AllocationService;
import service.ExamViewService;
import service.impl.AllocationServiceImpl;
import service.impl.ExamViewServiceImpl;

import java.io.IOException;

@WebServlet("/staff/examstaff/report")
public class ReportServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final ExamViewService examViewService = new ExamViewServiceImpl();
    private final AllocationService allocationService = new AllocationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Resolve the selected session id (request param wins, then session).
        String sessIdParam = request.getParameter("sessionId");
        int sessionId = 2; // Default session, matching the branch default.
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (NumberFormatException e) {
                // Keep the default when the param is not a valid number.
            }
        } else if (session.getAttribute("selectedSessionId") != null) {
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        session.setAttribute("selectedSessionId", sessionId);

        // Current session details for the report header.
        SessionViewDTO currentSession = allocationService.getSessionById(sessionId);
        request.setAttribute("currentSession", currentSession);

        // Build the end-of-day exam report statistics via the service.
        ExamReportDTO report = examViewService.buildExamReport(sessionId);
        request.setAttribute("report", report);

        request.getRequestDispatcher("/views/staff/examstaff/report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
