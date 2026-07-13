package controller.staff.examstaff;

import enums.AuditAction;
import enums.AuditEntity;
import enums.ExamStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Exam;
import model.User;
import service.AuditService;
import service.ExamService;
import service.impl.AuditServiceImpl;
import service.impl.ExamServiceImpl;

import java.io.IOException;

@WebServlet({"/staff/examstaff/exam-control", "/views/staff/examstaff/dashboard"})
public class ExamControlServlet extends HttpServlet {

    // Service layer only; controllers must never touch DAOs or DB connections.
    private final ExamService examService = new ExamServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int examId = parseExamId(request, session);
        int staffId = resolveStaffId(session);
        String redirect = buildRedirect(request, examId);

        if ("startSession".equals(action)) {
            // Start the exam: mark the exam as in progress.
            // Status persistence is handled by ExamService (lead-provided update); here we
            // record the action and surface the flash message.
            Exam exam = examService.getById(examId);
            if (exam != null) {
                exam.setStatus(ExamStatus.IN_PROGRESS.getValue());
            }
            String message = "Bắt đầu kỳ thi ExamId=" + examId;
            auditService.logAction(staffId, AuditAction.UPDATE, AuditEntity.EXAM_SESSION, message, examId);
            session.setAttribute("examControlMsg", message);
        } else if ("endSession".equals(action)) {
            // End the exam: mark the exam as completed.
            Exam exam = examService.getById(examId);
            if (exam != null) {
                exam.setStatus(ExamStatus.COMPLETED.getValue());
            }
            String message = "Kết thúc kỳ thi ExamId=" + examId;
            auditService.logAction(staffId, AuditAction.UPDATE, AuditEntity.EXAM_SESSION, message, examId);
            session.setAttribute("examControlMsg", message);
        }

        response.sendRedirect(redirect);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        int examId = parseExamId(request, session);

        // Keep the selected exam in sync for the sidebar and header.
        session.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedExamId", examId);

        // Load the current exam via the service layer.
        Exam currentExam = examService.getById(examId);
        request.setAttribute("currentExam", currentExam);

        // Normalize the status into an enum name for the view branching.
        String statusName = "UNKNOWN";
        String statusValue = "Không xác định";
        if (currentExam != null && currentExam.getStatus() != null) {
            ExamStatus status = ExamStatus.fromValue(currentExam.getStatus());
            if (status != null) {
                statusName = status.name();
                statusValue = status.getValue();
            }
        }
        request.setAttribute("sessionStatusName", statusName);
        request.setAttribute("sessionStatusValue", statusValue);

        // The session dropdown list and assigned-examiner count previously came from
        // SessionService, which is removed. These are intentionally dropped here; wire
        // them back through ExamService once the list/count methods are available.

        // Move one-shot flash messages from the session into the request.
        String msg = (String) session.getAttribute("examControlMsg");
        if (msg != null) {
            request.setAttribute("examControlMsg", msg);
            session.removeAttribute("examControlMsg");
        }
        String error = (String) session.getAttribute("sessionControlError");
        if (error != null) {
            request.setAttribute("sessionControlError", error);
            session.removeAttribute("sessionControlError");
        }

        request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
    }

    private int parseExamId(HttpServletRequest request, HttpSession session) {
        String param = request.getParameter("examId");
        if (param != null && !param.trim().isEmpty()) {
            try {
                return Integer.parseInt(param.trim());
            } catch (NumberFormatException ignored) {
                // Fall through to the session/default value.
            }
        }
        Integer selected = (Integer) session.getAttribute("selectedExamId");
        return selected != null ? selected : 2;
    }

    private int resolveStaffId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }

    private String buildRedirect(HttpServletRequest request, int examId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/views/staff/examstaff/examiner-allocation?examId=" + examId;
        }
        return ctx + "/views/staff/examstaff/dashboard?examId=" + examId;
    }
}
