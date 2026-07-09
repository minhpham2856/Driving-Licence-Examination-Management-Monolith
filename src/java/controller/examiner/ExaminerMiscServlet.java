package controller.examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamViewService;
import service.impl.ExamViewServiceImpl;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/views/examiner/audit",
    "/views/examiner/export",
    "/views/examiner/print-documents"
})
public class ExaminerMiscServlet extends BaseExaminerServlet {

    protected final ExamViewService viewDataService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        String path = stripContextPath(request);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/audit".equals(path)) {
                Map<String, Object> data = viewDataService.getAuditLogsData(sessionId, request.getParameter("page"), search);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            } else if ("/views/examiner/print-documents".equals(path)) {
                applyCandidateListAttributes(request, session, viewDataService, sessionId, sbd, search);
            }
        }
        String jsp = switch (path) {
            case "/views/examiner/audit" -> "/views/examiner/audit.jsp";
            case "/views/examiner/export" -> "/views/examiner/export.jsp";
            case "/views/examiner/print-documents" -> "/views/examiner/print-documents.jsp";
            default -> "/views/examiner/dashboard.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }
}
