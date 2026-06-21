package Controllers.Examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles miscellaneous simple view-only pages: audit, export, and print-documents.
@WebServlet(urlPatterns = {
    "/views/examiner/audit",
    "/views/examiner/export",
    "/views/examiner/print-documents"
})
public class ExaminerMiscServlet extends BaseExaminerServlet {

    // Renders the audit, export, or print-documents view.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/audit".equals(path)) {
                viewDataService.attachAuditLogs(request, sessionId, request.getParameter("page"), search);
            } else if ("/views/examiner/print-documents".equals(path)) {
                viewDataService.attachToRequest(request, sessionId, sbd, search);
            } else if ("/views/examiner/export".equals(path)) {
                // Export page has no specific dynamic data load beyond active session layout
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/audit" -> "/views/examiner/audit.jsp";
            case "/views/examiner/export" -> "/views/examiner/export.jsp";
            case "/views/examiner/print-documents" -> "/views/examiner/print-documents.jsp";
            default -> "/views/examiner/dashboard.jsp";
        };
        forward(request, response, jsp);
    }
}
