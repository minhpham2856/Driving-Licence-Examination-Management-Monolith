package examiner.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import examiner.filter.ExaminerFilter;
import examiner.service.ExamViewService;
import examiner.dto.CandidateRowDTO;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.service.CallService;
import examiner.service.impl.CallServiceImpl;
import shared.Attributes;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/examiner/audit",
    "/examiner/export",
    "/examiner/print-documents"
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
            if ("/examiner/audit".equals(path)) {
                Map<String, Object> data = viewDataService.getAuditLogsData(activeExamId, request.getParameter("page"), search);
                if (data != null) {
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            } else if ("/examiner/print-documents".equals(path) || "/examiner/export".equals(path)) {
                request.setAttribute("suspendedCandidates",
                        viewDataService.loadSuspendedCandidateRows(activeExamId));

                if ("/examiner/print-documents".equals(path) && sbd != null && sbd > 0) {
                    boolean isTheory = Boolean.TRUE.equals(session.getAttribute(Attributes.Examiner.IS_THEORY));
                    String sectionName = resolveSectionName(session);
                    CandidateRowDTO candidate = viewDataService.getCandidateViewRow(
                            activeExamId, sbd, isTheory, sectionName);
                    if (candidate != null) {
                        request.setAttribute(Attributes.Request.CANDIDATE, candidate);
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/examiner/audit" -> "/views/examiner/audit.jsp";
            case "/examiner/export" -> "/views/examiner/export.jsp";
            case "/examiner/print-documents" -> "/views/examiner/print-documents.jsp";
            default -> "/views/examiner/audit.jsp";
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
        Object name = session.getAttribute(Attributes.Examiner.EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }
}
