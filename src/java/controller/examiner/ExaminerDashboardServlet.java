package controller.examiner;

import dto.CandidateRowDTO;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExamViewService;
import service.impl.ExamViewServiceImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static util.FormatUtil.text;

@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends HttpServlet {

    private final ExamViewService examViewService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read session information prepared by ExaminerFilter
        Integer sessionId = (Integer) request.getAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID);
        boolean isTheory = Boolean.TRUE.equals(request.getAttribute(ExaminerFilter.ATTR_SECTION_THEORY));
        String sectionName = (String) request.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);

        // Read optional search and candidate selection parameters
        Integer candidateNumber = getCandidateNumber(request);
        String search = request.getParameter("q");

        // Load all candidates for the current session
        List<CandidateRowDTO> candidates
                = examViewService.loadCandidateRows(sessionId, isTheory, sectionName);

        // Apply search filter when a keyword is provided
        if (text(search) != null) {
            candidates = filterCandidates(candidates, search.trim());

            request.setAttribute("searchActive", true);
            request.setAttribute("searchQuery", search.trim());
        }

        // Provide candidate list and summary for the dashboard
        request.setAttribute("candidates", candidates);
        request.setAttribute("candidateQueue", candidates);
        request.setAttribute(
                "examSummary",
                examViewService.buildCandidateSummary(sessionId, isTheory, sectionName)
        );

        // Load detailed information for the selected candidate
        if (candidateNumber != null && candidateNumber > 0) {
            CandidateRowDTO candidate
                    = examViewService.getCandidateViewRow(sessionId, candidateNumber, isTheory, sectionName);

            if (candidate != null) {
                request.setAttribute("candidate", candidate);
            }
        }

        // Display the dashboard page
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }

    // Parse the candidate number from the request
    private Integer getCandidateNumber(HttpServletRequest request) {

        String value = request.getParameter("sbd");

        if (text(value) == null) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Filter candidates by SBD, full name or government ID
    private List<CandidateRowDTO> filterCandidates(List<CandidateRowDTO> candidates, String keyword) {

        String query = keyword.toLowerCase(Locale.ROOT);
        List<CandidateRowDTO> filtered = new ArrayList<>();

        for (CandidateRowDTO row : candidates) {

            String sbd = String.valueOf(row.getSbd()).toLowerCase(Locale.ROOT);
            String name = row.getFullName() == null
                    ? ""
                    : row.getFullName().toLowerCase(Locale.ROOT);
            String governmentId = row.getGovernmentId() == null
                    ? ""
                    : row.getGovernmentId().toLowerCase(Locale.ROOT);

            // Match against SBD, candidate name or government ID
            if (sbd.contains(query)
                    || name.contains(query)
                    || governmentId.contains(query)) {
                filtered.add(row);
            }
        }

        return filtered;
    }
}
