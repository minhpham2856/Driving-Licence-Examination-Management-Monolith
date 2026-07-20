package examiner.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examiner.filter.ExaminerFilter;
import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.SectionType;
import examiner.service.ActionService;
import examiner.service.ExamViewService;
import examiner.dto.CandidateRowDTO;
import examiner.dto.ServiceResult;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.ListUtil;
import static shared.util.FormatUtil.formatPositiveInt;
import static shared.util.FormatUtil.formatString;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {"/examiner/violations"})
// Violations page: lists candidates and supports one-click suspend or undo-suspend actions.
public class ViolationsServlet extends HttpServlet {

    private final ExamViewService viewService = new ExamViewServiceImpl();
    private final ActionService actionService = new ActionServiceImpl();

    // Serve the violations list with searchable candidate rows for the active exam session.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            // Search filter is applied in-memory after DB load on violations.jsp.
            String search = ListUtil.normalizeSearch(request.getParameter("q"));
            List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                    activeExamId, sectionType, formatString(search));
            ListUtil.applySortAndSearch(request, candidates);
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
        }
        // Forward even without active exam so JSP can show empty state.
        request.getRequestDispatcher("/views/examiner/violations.jsp").forward(request, response);
    }

    // Suspend or undo-suspend the selected candidate and redirect with outcome flash params.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int sbd = formatPositiveInt(request.getParameter("sbd"));
        if (sbd <= 0) {
            response.sendRedirect(request.getContextPath() + "/examiner/violations?error=noCandidateNumber");
            return;
        }
        UserDTO user = (UserDTO) session.getAttribute(Attributes.Session.USER);
        Integer userId = user != null ? user.getUserId() : null;
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        String action = request.getParameter("action");
        String redirect;
        // undoSuspend clears Candidate.IsSuspended; default action sets suspend flag.
        if ("undoSuspend".equals(action)) {
            ServiceResult<Void> result = actionService.undoSuspension(activeExamId, sbd, userId, sectionType);
            redirect = result.isSuccess()
                    ? "/examiner/violations?sbd=" + urlEncode(sbd) + "&unsuspended=1"
                    : "/examiner/violations?sbd=" + urlEncode(sbd) + "&error=unsuspendFailed";
        } else {
            // Default: one-click suspend (action=suspend or missing).
            ServiceResult<Void> result = actionService.markSuspended(activeExamId, sbd, userId, null, null,
                    sectionType);
            redirect = result.isSuccess()
                    ? "/examiner/violations?suspended=" + urlEncode(sbd)
                    : "/examiner/violations?sbd=" + urlEncode(sbd) + "&error=suspendFailed";
        }
        response.sendRedirect(request.getContextPath() + redirect);
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
