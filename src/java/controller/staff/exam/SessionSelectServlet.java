package controller.staff.exam;

import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.binder.ExamStaffPageBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
import controller.staff.exam.page.ExamStaffPageFacade;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffQueueRefreshInput;
import dto.examstaff.SessionSelectRequestDTO;
import dto.examstaff.SessionSelectResultDTO;
import enums.ExamStaffMessage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CandidateQueueService;
import service.ExamStaffServices;
import service.SessionSelectService;
import util.Utf8EncodingHelper;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/select-session")
public class SessionSelectServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final SessionSelectService sessionSelectService = SERVICES.sessionSelect();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    private void handleSelect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utf8EncodingHelper.apply(request, response);
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession httpSession = request.getSession();
        try {
            SessionSelectRequestDTO selectRequest = new SessionSelectRequestDTO();
            selectRequest.setUrlSessionId(ExamStaffHttpSupport.parseSessionIdParam(request));
            selectRequest.setPreviousExamId((Integer) httpSession.getAttribute("selectedExamId"));
            selectRequest.setPreviousSessionId((Integer) httpSession.getAttribute("selectedSessionId"));
            selectRequest.setWebRoot(request.getServletContext().getRealPath("/"));

            SessionSelectResultDTO result = sessionSelectService.processSelection(selectRequest);
            if (!result.isSuccess()) {
                httpSession.setAttribute("sessionSelectError", result.getErrorMessage());
                response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
                return;
            }

            selectionFacade.applySessionIdFromRequest(request, httpSession,
                    selectionFacade.loadAllSessions());

            if (result.isClearProcedureOnExamChange()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(httpSession,
                        result.getNewExamId(), result.getNewSessionId());
            } else if (result.isClearCandidateCache()) {
                selectionFacade.clearCandidateCache(httpSession);
            }

            refreshCandidateQueue(httpSession, result.getExamId(),
                    result.getSessionId(), selectRequest.getWebRoot(), selectionFacade.loadAllSessions());

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffSessionJustChanged", Boolean.TRUE);
            httpSession.setAttribute("sessionSelectMsg", ExamStaffMessage.SESSION_SELECTED.getText());

            String redirect = ExamStaffHttpSupport.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard");
            redirect = ExamStaffHttpSupport.stripQueryString(redirect);

            int pickerSessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
            if (pickerSessionId > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "sessionId", String.valueOf(pickerSessionId));
            } else if (result.getSessionId() > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "sessionId", String.valueOf(result.getSessionId()));
            }

            redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "_", String.valueOf(System.currentTimeMillis()));
            response.sendRedirect(redirect);
        } catch (Exception e) {
            e.printStackTrace();
            httpSession.setAttribute("sessionSelectError",
                    ExamStaffMessage.SESSION_CHANGE_ERROR_PREFIX.getText()
                            + (e.getMessage() != null ? e.getMessage() : ExamStaffMessage.UNKNOWN_ERROR.getText()));
            response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
        }
    }

    private void refreshCandidateQueue(HttpSession session, int examId, int sessionId,
            String webRoot, java.util.List<dto.SessionDTO> allSessions) {
        if (session == null) {
            return;
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setSessionId(sessionId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
        @SuppressWarnings("unchecked")
        java.util.List<String> order = (java.util.List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
    }
}
