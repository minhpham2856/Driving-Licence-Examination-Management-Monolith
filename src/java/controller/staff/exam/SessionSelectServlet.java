package controller.staff.exam;

import controller.staff.exam.support.ExamStaffHttpSupport;
import dto.examstaff.SessionSelectRequestDTO;
import dto.examstaff.SessionSelectResultDTO;
import enums.ExamStaffMessage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.SessionSelectService;
import service.impl.SessionSelectServiceImpl;
import util.Utf8EncodingHelper;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/select-session")
public class SessionSelectServlet extends HttpServlet {

    private final SessionSelectService sessionSelectService = new SessionSelectServiceImpl();

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

            ExamStaffViewHelper.applySessionIdFromRequest(request, httpSession,
                    ExamStaffViewHelper.loadAllSessions());

            if (result.isClearProcedureOnExamChange()) {
                ExamStaffViewHelper.clearProcedureStateOnExamChange(request, httpSession,
                        result.getPreviousExamId(), result.getPreviousSessionId(),
                        result.getNewExamId(), result.getNewSessionId());
            } else if (result.isClearCandidateCache()) {
                ExamStaffViewHelper.clearCandidateCache(httpSession);
            }

            ExamStaffViewHelper.refreshCandidateQueue(httpSession, result.getExamId(),
                    result.getSessionId(), selectRequest.getWebRoot(), ExamStaffViewHelper.loadAllSessions());

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
}
