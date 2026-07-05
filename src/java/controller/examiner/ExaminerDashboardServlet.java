package controller.examiner;

import dto.ExaminerCandidateRowDTO;
import dto.payload.CandidateCallDataDTO;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import util.ExaminerCandidateSort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends BaseExaminerServlet {

    private final ExaminerDataService dataService = new ExaminerDataServiceImpl();

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
            List<ExaminerCandidateRowDTO> candidates = dataService.loadCandidateRows(sessionId, isTheory, sectionName);
            ExaminerCandidateSort.applyCandidateSort(request, candidates);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(Locale.ROOT);
                List<ExaminerCandidateRowDTO> filtered = new ArrayList<>();
                for (ExaminerCandidateRowDTO row : candidates) {
                    String sbdVal = String.valueOf(row.getSbd());
                    String name = row.getFullName() != null ? row.getFullName() : "";
                    String gov = row.getGovernmentId() != null ? row.getGovernmentId() : "";
                    if (sbdVal.toLowerCase(Locale.ROOT).contains(q)
                            || name.toLowerCase(Locale.ROOT).contains(q)
                            || gov.toLowerCase(Locale.ROOT).contains(q)) {
                        filtered.add(row);
                    }
                }
                candidates = filtered;
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", search.trim());
            }
            request.setAttribute("candidates", candidates);
            request.setAttribute("examSummary", dataService.buildCandidateSummary(sessionId, isTheory, sectionName));
            CandidateCallDataDTO data = dataService.getCandidateCallData(sessionId, sbd, search);
            if (data.getCandidate() != null) {
                request.setAttribute("candidate", data.getCandidate());
            }
            if (data.isSearchActive()) {
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", data.getSearchQuery());
            }
        }
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }
}
