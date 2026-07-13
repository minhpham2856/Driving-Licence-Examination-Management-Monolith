package examiner.controller;

import static shared.util.FormatUtil.formatPositiveInteger;
import static shared.util.FormatUtil.formatString;
import shared.enums.SectionType;
import examiner.filter.ExaminerFilter;
import auth.dto.UserDTO;
import shared.Attributes;
import examiner.service.ExamViewService;
import examiner.dto.CandidateRowDTO;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.service.ActionService;
import examiner.service.impl.ActionServiceImpl;
import examiner.util.ListUtil;
import shared.model.ExaminerSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/examiner/action")

// Actions controller: displays the candidate queue and handles workflow actions (present, suspend, print, complete section).
public class ActionServlet extends HttpServlet {

    private final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Serve the call-board page with queue-ordered candidate rows and exam summary stats.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Require an authenticated session; call-board is not public.
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Load queue data only when the examiner has selected an active exam session.
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId != null && activeExamId > 0) {
            // Section type (theory vs layout) drives eligibility rules and queue lane.
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);

            // Optional search narrows rows before queue ordering is applied.
            String search = ListUtil.normalizeSearch(request.getParameter("q"));
            List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                    activeExamId, sectionType, formatString(search));
            // Re-order rows to match ExamRoomQueueRegistry display order for this exam area.
            int examAreaId = 0;
            ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
            if (schedule != null && schedule.getExamAreaId() != null) {
                examAreaId = schedule.getExamAreaId();
            }
            candidates = viewService.orderCandidateRowsByQueue(
                    candidates, activeExamId, examAreaId, sectionType);
            ListUtil.applySortAndSearch(request, candidates);

            // Attributes consumed by action.jsp for the table and summary header.
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            request.setAttribute("examSummary", viewService.getStatsByExam(activeExamId, sectionType));
        }
        request.getRequestDispatcher("/views/examiner/action.jsp").forward(request, response);
    }

    // Process a POST action for the selected candidate; fall back to GET when the action name is unrecognized.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // POST actions require a bound exam id from exam-select flow.
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing active exam");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/examiner/action?error=noAction");
            return;
        }

        // Target candidate for row-level actions (present, suspend, complete, etc.).
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        // Known actions redirect; unknown actions fall through to doGet refresh.
        if (handleAction(request, response, session, activeExamId, action, sbd)) {
            return;
        }
        doGet(request, response);
    }

    // Dispatch a named call-board action to ActionService and redirect with success or error query params.
    private boolean handleAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int activeExamId, String action, Integer sbd) throws IOException {
        UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
        int userId = userDto.getUserId();
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);

        // Each branch delegates to ActionService then redirects with flash query params for action.jsp.
        switch (action) {
            case "undoPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.undoPresent(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=undoPresentFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?undoPresent="
                        + urlEncode(sbd));
                return true;
            }
            case "markPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.markPresent(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=presentFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?presentDone="
                        + urlEncode(sbd));
                return true;
            }
            case "wrongInfo" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.sendWrongInfoToProcedure(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=wrongInfoFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?wrongInfoDone="
                        + urlEncode(sbd));
                return true;
            }
            case "printResult", "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.printResultForm(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=resultPrintFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/print?type=result&sbd="
                        + urlEncode(sbd));
                return true;
            }
            case "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> res = actionService.completeCandidateSection(
                        activeExamId, sbd, userId, null, sectionType);
                if (res != null && "needResultPrint".equals(res.getMessage())) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=needResultPrint&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                if (res != null && !res.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=completeFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?completeDone="
                        + urlEncode(sbd));
                return true;
            }
            case "suspend" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.markSuspended(activeExamId, sbd, userId, null, null, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=suspendFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?suspended="
                        + urlEncode(sbd));
                return true;
            }
            case "undoSuspend" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                if (!actionService.undoSuspension(activeExamId, sbd, userId, sectionType).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=unsuspendFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?unsuspended="
                        + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
