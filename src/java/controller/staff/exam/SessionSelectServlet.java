package controller.staff.exam;

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
import util.Utf8EncodingUtil;

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
        Utf8EncodingUtil.apply(request, response);
        BaseExamStaffServlet.applyNoCacheHeaders(response);
        HttpSession httpSession = request.getSession();
        try {
            SessionSelectRequestDTO selectRequest = new SessionSelectRequestDTO();
            selectRequest.setUrlSessionId(BaseExamStaffServlet.parseSessionIdParam(request));
            selectRequest.setPreviousExamId((Integer) httpSession.getAttribute("selectedExamId"));
            selectRequest.setPreviousSessionId((Integer) httpSession.getAttribute("selectedSessionId"));
            selectRequest.setWebRoot(request.getServletContext().getRealPath("/"));

            SessionSelectResultDTO result = sessionSelectService.processSelection(selectRequest);
            if (!result.isSuccess()) {
                httpSession.setAttribute("sessionSelectError", result.getErrorMessage());
                response.sendRedirect(BaseExamStaffServlet.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
                return;
            }

            BaseExamStaffServlet.applySessionIdFromRequest(request, httpSession,
                    BaseExamStaffServlet.loadAllSessions());

            if (result.isClearProcedureOnExamChange()) {
                BaseExamStaffServlet.clearProcedureStateOnExamChange(request, httpSession,
                        result.getPreviousExamId(), result.getPreviousSessionId(),
                        result.getNewExamId(), result.getNewSessionId());
            } else if (result.isClearCandidateCache()) {
                BaseExamStaffServlet.clearCandidateCache(httpSession);
            }

            BaseExamStaffServlet.refreshCandidateQueue(httpSession, result.getExamId(),
                    result.getSessionId(), selectRequest.getWebRoot(), BaseExamStaffServlet.loadAllSessions());

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffSessionJustChanged", Boolean.TRUE);
            httpSession.setAttribute("sessionSelectMsg", ExamStaffMessage.SESSION_SELECTED.getText());

            String redirect = BaseExamStaffServlet.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard");
            redirect = BaseExamStaffServlet.stripQueryString(redirect);

            int pickerSessionId = BaseExamStaffServlet.parseSessionIdParam(request);
            if (pickerSessionId > 0) {
                redirect = BaseExamStaffServlet.upsertQueryParam(redirect, "sessionId", String.valueOf(pickerSessionId));
            } else if (result.getSessionId() > 0) {
                redirect = BaseExamStaffServlet.upsertQueryParam(redirect, "sessionId", String.valueOf(result.getSessionId()));
            }

            redirect = BaseExamStaffServlet.upsertQueryParam(redirect, "_", String.valueOf(System.currentTimeMillis()));
            response.sendRedirect(redirect);
        } catch (Exception e) {
            e.printStackTrace();
            httpSession.setAttribute("sessionSelectError",
                    ExamStaffMessage.SESSION_CHANGE_ERROR_PREFIX.getText()
                            + (e.getMessage() != null ? e.getMessage() : ExamStaffMessage.UNKNOWN_ERROR.getText()));
            response.sendRedirect(BaseExamStaffServlet.resolveSafeRedirect(request, "/views/staff/examstaff/dashboard"));
        }
    }
}
