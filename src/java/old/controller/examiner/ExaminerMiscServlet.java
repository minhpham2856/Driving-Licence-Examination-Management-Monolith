package controller.examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import filter.ExaminerFilter;
import service.ExamViewService;
import dto.CandidateRowDTO;
import service.impl.ExamViewServiceImpl;
import service.CallService;
import service.impl.CallServiceImpl;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/old_views/examiner/audit",
    "/old_views/examiner/export",
    "/old_views/examiner/print-documents"
})
public class ExaminerMiscServlet extends HttpServlet {
    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService callService = new CallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String path = stripContextPath(request);
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {}
        
        String search = request.getParameter("q");

        if (activeExamId != null && activeExamId > 0) {
            if ("/old_views/examiner/audit".equals(path)) {
                Map<String, Object> data = viewDataService.getAuditLogsData(activeExamId, request.getParameter("page"), search);
                if (data != null) {
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            } else if ("/old_views/examiner/print-documents".equals(path)) {
                boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
                String sectionName = resolveSectionName(session);
                
                if (sbd != null && sbd > 0) {
                    CandidateRowDTO candidate = viewDataService.getCandidateViewRow(activeExamId, sbd, isTheory, sectionName);
                    if (candidate != null) {
                        request.setAttribute("candidate", candidate);
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/old_views/examiner/audit" -> "/old_views/examiner/audit.jsp";
            case "/old_views/examiner/export" -> "/old_views/examiner/export.jsp";
            case "/old_views/examiner/print-documents" -> "/old_views/examiner/print-documents.jsp";
            default -> "/old_views/examiner/audit.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
    
    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute("examSectionName");
        return name != null ? String.valueOf(name) : null;
    }
}
