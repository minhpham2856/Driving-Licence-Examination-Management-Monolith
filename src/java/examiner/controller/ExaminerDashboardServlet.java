package examiner.controller;

import examiner.dto.CandidateRowDTO;
import examiner.enums.SectionType;
import static examiner.enums.SectionType.THEORY;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import examiner.service.ExamViewService;
import examiner.service.impl.ExamViewServiceImpl;
import java.io.IOException;
import java.util.List;
import static util.FormatUtil.text;

@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends HttpServlet {

    private final ExamViewService examViewService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read session information prepared by ExaminerFilter
        Integer examId = (Integer) request.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        SectionType section = (SectionType) request.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        boolean isTheory = section == THEORY;
        String sectionName = section.getValue();

        // Read optional search and candidate selection parameters
        Integer candidateNumber = getCandidateNumber(request);
        String search = request.getParameter("q");

        // Load candidate rows for the current session, filtered at the database when searching
        List<CandidateRowDTO> candidates
                = examViewService.loadCandidateRows(examId, isTheory, sectionName, text(search));

        // Flag that a search is active so the view can show the query and a clear button
        if (text(search) != null) {
            request.setAttribute("searchActive", true);
            request.setAttribute("searchQuery", search.trim());
        }

        // Provide candidate list and summary for the dashboard
        request.setAttribute("candidates", candidates);
        request.setAttribute("candidateQueue", candidates);
        request.setAttribute(
                "examSummary",
                examViewService.buildCandidateSummary(examId, isTheory, sectionName)
        );

        // Load detailed information for the selected candidate
        if (candidateNumber != null && candidateNumber > 0) {
            CandidateRowDTO candidate
                    = examViewService.getCandidateViewRow(examId, candidateNumber, isTheory, sectionName);

            if (candidate != null) {
                request.setAttribute("candidate", candidate);
            }
        }

        // Display the dashboard page
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }

    // Parse the candidate number from the request
    private Integer getCandidateNumber(HttpServletRequest request) {
        String value = request.getParameter("candidateNumber");

        if (text(value) == null) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
