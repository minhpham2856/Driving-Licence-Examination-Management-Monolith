package controller.examiner;

import dto.CandidateRowDTO;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamViewService;
import service.impl.ExamViewServiceImpl;
import util.ExaminerCandidateSort;

import java.io.IOException;
import java.util.List;

@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends BaseExaminerServlet {

    private final ExamViewService dataService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = getActiveSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        if (sessionId != null && sessionId > 0) {
            boolean isTheory = ExaminerFilter.isTheorySession(session);
            String sectionName = getSectionDisplayName(session);
            List<CandidateRowDTO> candidates = dataService.loadCandidateRows(sessionId, isTheory, sectionName);
            if (search != null && !search.isBlank()) {
                candidates = dataService.filterCandidateRows(candidates, search);
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", search.trim());
            }
            applyCandidateSort(request, candidates);
            request.setAttribute("candidates", candidates);
            request.setAttribute("examSummary", dataService.buildCandidateSummary(sessionId, isTheory, sectionName));
            if (sbd != null && sbd > 0) {
                CandidateRowDTO candidate = dataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
                if (candidate != null) {
                    request.setAttribute("candidate", candidate);
                }
            }
        }
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }
}
