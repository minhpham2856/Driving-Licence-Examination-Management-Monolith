package controller.examiner;
import filter.ExaminerPortalFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;
import java.io.IOException;
import java.util.Map;
@WebServlet(urlPatterns = {
    "/views/examiner/audit",
    "/views/examiner/export",
    "/views/examiner/print-documents"
})
public class ExaminerMiscServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
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
                Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            } else if ("/views/examiner/export".equals(path)) {
            }
        }
        String jsp = switch (path) {
            case "/views/examiner/audit" ->
                "/views/examiner/audit.jsp";
            case "/views/examiner/export" ->
                "/views/examiner/export.jsp";
            case "/views/examiner/print-documents" ->
                "/views/examiner/print-documents.jsp";
            default ->
                "/views/examiner/dashboard.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
    private Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
