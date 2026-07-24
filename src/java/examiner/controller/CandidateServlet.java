package examiner.controller;

import examiner.filter.ExaminerFilter;
import examiner.service.ExamViewService;
import examiner.dto.CandidateRowDTO;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.ListUtil;
import examiner.util.RequestUtil;
import shared.Attributes;
import static shared.util.FormatUtil.formatPositiveInteger;
import static shared.util.FormatUtil.formatString;
import shared.enums.SectionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/examiner/candidates",
    "/examiner/candidate-details",
    "/examiner/candidate-paper"
})
// Candidate views: list all candidates, show profile details, or display theory paper answers by URL path.
public class CandidateServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();

    // Load candidate list, detail, or theory paper data based on the matched URL path and forward to the JSP.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        String path = RequestUtil.stripContextPath(request);
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        String search = request.getParameter("q");

        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);

            if ("/examiner/candidates".equals(path)) {
                // Candidate details list only needs lightweight row data plus action buttons.
                String normalizedSearch = ListUtil.normalizeSearch(search);
                List<CandidateRowDTO> candidates = viewService.getActionCandidateListByExam(
                        activeExamId, sectionType, formatString(normalizedSearch));
                ListUtil.applySortAndSearch(request, candidates);
                request.setAttribute(Attributes.Request.CANDIDATES, candidates);
            }

            if (sbd != null && sbd > 0) {
                CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
                if (candidate != null) {
                    request.setAttribute(Attributes.Request.CANDIDATE, candidate);
                }

                if ("/examiner/candidate-paper".equals(path)) {
                    // Theory paper view needs answer map keyed for candidate-paper.jsp.
                    Map<String, Object> ansData = viewService.getPaperAnswersData(activeExamId, sbd, request.getContextPath());
                    RequestUtil.applyModel(request, ansData);
                }
            }
        }

        String jsp = switch (path) {
            case "/examiner/candidates" ->
                "/views/examiner/candidates.jsp";
            case "/examiner/candidate-details" ->
                "/views/examiner/candidate-details.jsp";
            case "/examiner/candidate-paper" ->
                "/views/examiner/candidate-paper.jsp";
            default ->
                "/views/examiner/candidates.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }
}
