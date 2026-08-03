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
import examiner.util.RequestUtil;
import shared.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;

@WebServlet("/examiner/action")

// Actions controller: displays the section candidate list and handles workflow actions.
public class ActionServlet extends HttpServlet {

    private final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Serve the action page with candidates from the active exam and section.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Require an authenticated session; call-board is not public.
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Load action data only when the examiner has selected an active exam session.
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        if (activeExamId != null && activeExamId > 0) {
            // Section type (theory vs layout) drives eligibility rules.
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);

            // Optional search narrows rows at enrollment query level.
            String search = ListUtil.normalizeSearch(request.getParameter("q"));
            List<CandidateRowDTO> candidates = viewService.getActionCandidateListByExam(
                    activeExamId, sectionType, formatString(search));
            ListUtil.applySortAndSearch(request, candidates);

            request.setAttribute(Attributes.Request.CANDIDATES, candidates);
        }
        ExaminerFlash.bind(request);
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
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
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

    // Dispatch a named row action to ActionService and redirect with success or error query params.
    private boolean handleAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int activeExamId, String action, Integer sbd) throws IOException {
        UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
        int userId = userDto.getUserId();
        User user = userDto.toUser();
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);

        // Each branch delegates to ActionService then redirects with flash query params for action.jsp.
        switch (action) {
            case "call" -> {
                examiner.dto.ServiceResult<Void> callResult = sbd == null ? null
                        : actionService.actionCandidate(activeExamId, sbd, user, userId, sectionType, "Khu vực thi");
                if (callResult == null || !callResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(callResult, "callFailed")));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?called=" + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "undoPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> undoResult =
                        actionService.undoPresent(activeExamId, sbd, userId, sectionType);
                if (!undoResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(undoResult, "undoPresentFailed"))
                            + "&sbd=" + RequestUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?undoPresent="
                        + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "markPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> presentResult =
                        actionService.markPresent(activeExamId, sbd, userId, sectionType);
                if (!presentResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(presentResult, "presentFailed"))
                            + "&sbd=" + RequestUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?presentDone="
                        + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "wrongInfo" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> wrongInfoResult =
                        actionService.sendWrongInfoToProcedure(activeExamId, sbd, userId, sectionType);
                if (!wrongInfoResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(wrongInfoResult, "wrongInfoFailed"))
                            + "&sbd=" + RequestUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?wrongInfoDone="
                        + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "printResult", "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> printResult =
                        actionService.printResultForm(activeExamId, sbd, userId, sectionType);
                if (!printResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(printResult, "resultPrintFailed"))
                            + "&sbd=" + RequestUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/print?type=result&sbd="
                        + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                examiner.dto.ServiceResult<Void> res = actionService.completeCandidateSection(
                        activeExamId, sbd, userId, null, sectionType);
                if (res != null && !res.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error="
                            + RequestUtil.urlEncode(errorCodeOrDefault(res, "completeFailed"))
                            + "&sbd=" + RequestUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/action?completeDone="
                        + RequestUtil.urlEncode(sbd));
                return true;
            }
            case "suspend" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/violations?sbd="
                        + RequestUtil.urlEncode(sbd) + "&mode=create&from=action");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // Prefer the service's specific error message/code over a generic fallback.
    private static String errorCodeOrDefault(examiner.dto.ServiceResult<Void> result, String fallback) {
        String message = result != null ? result.getMessage() : null;
        return (message != null && !message.isBlank()) ? message.trim() : fallback;
    }
}
