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
import examiner.service.ActionService;
import examiner.service.impl.ActionServiceImpl;
import shared.enums.SectionType;
import static shared.util.FormatUtil.formatPositiveInteger;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/examiner/audit",
    "/examiner/export",
    "/examiner/print-documents"
})
// Miscellaneous pages: audit log viewer and the unified print/export document selector.
public class MiscServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Route to audit, export hub, or print-documents view and load page-specific data for the active session.
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
        if ("/examiner/export".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/examiner/print-documents");
            return;
        }
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        String search = request.getParameter("q");

        if (activeExamId != null && activeExamId > 0) {
            // Each URL path loads different hub data for audit or print-documents picker.
            if ("/examiner/audit".equals(path)) {
                Map<String, Object> data = viewService.getAuditViewByExam(activeExamId, request.getParameter("page"), search);
                if (data != null) {
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            } else if ("/examiner/print-documents".equals(path)) {
                SectionType sectionType = ExaminerFilter.resolveSectionType(session);

                if (sbd != null && sbd > 0) {
                    CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
                    if (candidate != null) {
                        request.setAttribute("candidate", candidate);
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/examiner/audit" ->
                "/views/examiner/audit.jsp";
            case "/examiner/print-documents" ->
                "/views/examiner/print-documents.jsp";
            default ->
                "/views/examiner/audit.jsp";
        };
        // audit and print-documents need server-loaded attributes.
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    // Strip the servlet context path prefix from the request URI for multi-path routing.
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
}
