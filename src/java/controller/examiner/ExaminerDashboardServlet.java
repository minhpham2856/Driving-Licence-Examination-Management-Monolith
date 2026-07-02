package controller.examiner;
import dto.ExaminerSlotDTO;
import filter.ExaminerPortalFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;
import util.ExaminerCandidateSort;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@WebServlet("/views/examiner/dashboard")
public class ExaminerDashboardServlet extends HttpServlet {
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        if (sessionId != null && sessionId > 0) {
            boolean isTheory = ExaminerPortalFilter.isTheorySession(session);
            String sectionName = resolveSectionName(session);
            List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            ExaminerCandidateSort.applyCandidateSort(request, candidates);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(java.util.Locale.ROOT);
                List<Map<String, Object>> filtered = new java.util.ArrayList<>();
                for (Map<String, Object> row : candidates) {
                    String sbdVal = String.valueOf(row.get("sbd"));
                    String name = String.valueOf(row.get("fullName"));
                    String gov = String.valueOf(row.get("governmentId"));
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
            request.setAttribute("examSummary", viewDataService.buildCandidateSummary(sessionId, isTheory, sectionName));
            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                if (!"candidates".equals(mapEntry.getKey())) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
        request.getRequestDispatcher("/views/examiner/dashboard.jsp").forward(request, response);
    }
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
    private Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }
}
