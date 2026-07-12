package examstaff.controller;

import examstaff.util.ExamStaffHttpSupport;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamSelectRequestDTO;
import examstaff.dto.ExamSelectResultDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ExamSelectServiceImpl;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.Utf8EncodingHelper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.enums.ExamStaffMessage;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/select-exam")
public class ExamStaffExamSelectServlet extends HttpServlet {

    private final ExamSelectServiceImpl examSelectService = new ExamSelectServiceImpl();
    private final CandidateQueueServiceImpl candidateQueueService = new CandidateQueueServiceImpl();

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
            ExamSelectRequestDTO selectRequest = new ExamSelectRequestDTO();
            selectRequest.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
            selectRequest.setPreviousExamId(ExamStaffPageSupport.readSelectedExamId(httpSession));
            selectRequest.setWebRoot(request.getServletContext().getRealPath("/"));

            ExamSelectResultDTO result = examSelectService.processSelection(selectRequest);
            if (!result.isSuccess()) {
                httpSession.setAttribute("sessionSelectError", result.getErrorMessage());
                response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request,
                        "/views/staff/examstaff/dashboard"));
                return;
            }

            ExamStaffPageSupport.persistExamSelection(httpSession, result.getExamId(), result.getExamId());

            if (result.isClearProcedureOnExamChange()) {
                ExamStaffPageSupport.clearProcedureStateOnExamChange(httpSession,
                        result.getNewExamId(), result.getNewExamId());
            } else if (result.isClearCandidateCache()) {
                ExamStaffPageSupport.clearCandidateCache(httpSession);
            }

            refreshCandidateQueue(httpSession, result.getExamId(), selectRequest.getWebRoot());

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffSessionJustChanged", Boolean.TRUE);
            httpSession.setAttribute("sessionSelectMsg", ExamStaffMessage.SESSION_SELECTED.getValue());

            String redirect = ExamStaffHttpSupport.resolveSafeRedirect(request,
                    "/views/staff/examstaff/dashboard");
            redirect = ExamStaffHttpSupport.stripQueryString(redirect);

            int pickerExamId = ExamStaffHttpSupport.parseExamIdParam(request);
            if (pickerExamId > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "examId", String.valueOf(pickerExamId));
            } else if (result.getExamId() > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "examId", String.valueOf(result.getExamId()));
            }

            redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "_", String.valueOf(System.currentTimeMillis()));
            response.sendRedirect(redirect);
        } catch (Exception e) {
            e.printStackTrace();
            httpSession.setAttribute("sessionSelectError",
                    ExamStaffMessage.SESSION_CHANGE_ERROR_PREFIX.getValue()
                            + (e.getMessage() != null ? e.getMessage() : ExamStaffMessage.UNKNOWN_ERROR.getValue()));
            response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request,
                    "/views/staff/examstaff/dashboard"));
        }
    }

    private void refreshCandidateQueue(HttpSession session, int examId, String webRoot) {
        if (session == null) {
            return;
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllSessions(ExamStaffPageSupport.loadAllExams());
        input.setSelectedExamId(ExamStaffPageSupport.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        java.util.List<String> order = (java.util.List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageSupport.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageSupport.publishQueue(null, session, snapshot);
    }
}
