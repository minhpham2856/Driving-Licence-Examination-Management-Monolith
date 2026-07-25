package examiner.controller;

import static shared.util.FormatUtil.formatPositiveInteger;
import static shared.util.FormatUtil.formatString;
import examiner.dto.CandidateRowDTO;
import shared.enums.SectionType;
import shared.Attributes;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examiner.service.ExamViewService;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.ListUtil;
import java.io.IOException;
import java.util.List;

@WebServlet("/examiner/dashboard")

// Dashboard controller: candidate list with search, optional detail panel, and exam summary for the active session.
public class DashboardServlet extends HttpServlet {

    private final ExamViewService examViewService = new ExamViewServiceImpl();

    // Load candidate rows and optional detail for the active exam session and forward to dashboard.jsp.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read session information prepared by ExaminerFilter
        HttpSession session = request.getSession(false);
        Integer examId = (Integer) request.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        SectionType sectionType = null;
        Object rawSection = request.getAttribute(Attributes.Examiner.EXAM_SECTION);
        if (rawSection instanceof SectionType) {
            sectionType = (SectionType) rawSection;
        } else if (session != null) {
            sectionType = ExaminerFilter.resolveSectionType(session);
        }
        if (examId == null || sectionType == null) {
            response.sendRedirect(request.getContextPath() + "/examiner/exam");
            return;
        }

        // Read optional search and candidate selection params
        Integer candidateNumber = getCandidateNumber(request);
        String search = request.getParameter("q");

        // Dashboard must stay fast: one simple candidate-by-exam query only.
        List<CandidateRowDTO> candidates = examViewService.getDashboardCandidateListByExam(
                examId, sectionType, formatString(search));
        ListUtil.applySortAndSearch(request, candidates);

        // Provide candidate list and summary for the dashboard
        request.setAttribute(Attributes.Request.CANDIDATES, candidates);
        request.setAttribute(Attributes.Examiner.EXAM_SUMMARY,
                examViewService.getStatsByCandidateRows(examId, sectionType, candidates));

        // Load detailed information for the selected candidate
        if (candidateNumber != null && candidateNumber > 0) {
            CandidateRowDTO candidate = examViewService.getCandidateViewRow(
                    examId,
                    candidateNumber,
                    sectionType);

            if (candidate != null) {
                request.setAttribute(Attributes.Request.CANDIDATE, candidate);
            }
        }

        // Display the dashboard page
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }

    // Parse the candidate number from the request
    private Integer getCandidateNumber(HttpServletRequest request) {
        return formatPositiveInteger(request.getParameter("candidateNumber"));
    }
}
