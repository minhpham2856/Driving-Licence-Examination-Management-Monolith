package controller.staff.examstaff;

import dto.ExamReportDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Exam;
import service.AllocationService;
import service.ExamService;
import service.ExamViewService;
import service.impl.AllocationServiceImpl;
import service.impl.ExamServiceImpl;
import service.impl.ExamViewServiceImpl;

import java.io.IOException;

@WebServlet("/staff/examstaff/report")
public class ReportServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final ExamViewService examViewService = new ExamViewServiceImpl();
    private final AllocationService allocationService = new AllocationServiceImpl();
    private final ExamService examService = new ExamServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Resolve the selected session id (request param wins, then session).
        String sessIdParam = request.getParameter("examId");
        int examId = 2; // Default session, matching the branch default.
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                examId = Integer.parseInt(sessIdParam);
            } catch (NumberFormatException e) {
                // Keep the default when the param is not a valid number.
            }
        } else if (session.getAttribute("selectedExamId") != null) {
            examId = (Integer) session.getAttribute("selectedExamId");
        }
        session.setAttribute("selectedExamId", examId);

        // Current exam details for the report header.
        Exam currentExam = examService.getById(examId);
        request.setAttribute("currentExam", currentExam);

        // Build the end-of-day exam report statistics via the service.
        ExamReportDTO report = examViewService.buildExamReport(examId);
        request.setAttribute("report", report);

        request.getRequestDispatcher("/views/staff/examstaff/report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
