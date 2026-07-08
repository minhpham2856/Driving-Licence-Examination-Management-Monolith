package controller.examiner;

import dto.CandidateRowDTO;
import filter.ExaminerFilter;
import service.ExamViewService;
import service.impl.ExamViewServiceImpl;
import util.ExaminerCandidateSort;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/views/examiner/candidate-details",
    "/views/examiner/candidate-details-edit",
    "/views/examiner/candidate-paper"
})
public class ExaminerCandidateDetailsServlet extends BaseExaminerServlet {

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
            boolean isTheory = ExaminerFilter.isTheorySession(session);
            String sectionName = getSectionDisplayName(session);
            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            if (search != null && !search.isBlank()) {
                candidates = viewDataService.filterCandidateRows(candidates, search);
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", search.trim());
            }
            applyCandidateSort(request, candidates);
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            if (sbd != null && sbd > 0) {
                CandidateRowDTO candidate = viewDataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
                if (candidate != null) {
                    request.setAttribute("candidate", candidate);
                }
            }
            if ("/views/examiner/candidate-paper".equals(path)) {
                int paperSbd = sbd != null ? sbd : 0;
                Map<String, Object> ansData = viewDataService.getPaperAnswersData(sessionId, paperSbd, request.getContextPath());
                for (Map.Entry<String, Object> mapEntry : ansData.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
        String jsp = switch (path) {
            case "/views/examiner/candidate-details" -> "/views/examiner/candidate-details.jsp";
            case "/views/examiner/candidate-details-edit" -> "/views/examiner/candidate-details-edit.jsp";
            case "/views/examiner/candidate-paper" -> "/views/examiner/candidate-paper.jsp";
            default -> "/views/examiner/candidate-details.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = stripContextPath(request);
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Giám khảo không được sửa thông tin thí sinh.");
            return;
        }
        doGet(request, response);
    }
}
