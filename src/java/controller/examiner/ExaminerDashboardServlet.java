package controller.examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExamViewService;
import dto.CandidateRowDTO;
import service.impl.ExamViewServiceImpl;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends HttpServlet {

    private final ExamViewService viewDataService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.valueOf(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);

            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);

            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(java.util.Locale.ROOT);
                List<CandidateRowDTO> filtered = new java.util.ArrayList<>();
                for (CandidateRowDTO row : candidates) {
                    String sbdVal = String.valueOf(row.getSbd());
                    String name = row.getFullName() != null ? row.getFullName() : "";
                    String gov = row.getGovernmentId() != null ? row.getGovernmentId() : "";
                    if (sbdVal.toLowerCase(java.util.Locale.ROOT).contains(q)
                            || name.toLowerCase(java.util.Locale.ROOT).contains(q)
                            || gov.toLowerCase(java.util.Locale.ROOT).contains(q)) {
                        filtered.add(row);
                    }
                }
                candidates = filtered;
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", search.trim());
            }

            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            request.setAttribute("examSummary", viewDataService.buildCandidateSummary(sessionId, isTheory, sectionName));

            if (sbd != null && sbd > 0) {
                CandidateRowDTO candidate = viewDataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
                if (candidate != null) {
                    request.setAttribute("candidate", candidate);
                }
            }
        }

        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }

    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute("examSectionName");
        return name != null ? String.valueOf(name) : null;
    }
}
