package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamSelectRequestDTO;
import examstaff.dto.ExamSelectResultDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.enums.ExamStaffMessage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.service.ExamStaffServices;
import examstaff.util.Utf8EncodingHelper;

import java.io.IOException;

@WebServlet("/examstaff/select-exam")
public class ExamSelectServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamStaffSelectionService examSelectService = SERVICES.selection();
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
            ExamSelectRequestDTO selectRequest = new ExamSelectRequestDTO();
            selectRequest.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
            selectRequest.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(httpSession));
            selectRequest.setWebRoot(request.getServletContext().getRealPath("/"));

            ExamSelectResultDTO result = examSelectService.processSelection(selectRequest);
            if (!result.isSuccess()) {
                httpSession.setAttribute("examSelectError", result.getErrorMessage());
                response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard"));
                return;
            }

            selectionFacade.applyExamIdFromRequest(request, httpSession,
                    selectionFacade.loadAllExams());

            if (result.isClearProcedureOnExamChange()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(httpSession,
                        result.getNewExamId(), result.getNewExamId());
            } else if (result.isClearCandidateCache()) {
                selectionFacade.clearCandidateCache(httpSession);
            }

            refreshCandidateQueue(httpSession, result.getExamId(),
                    selectRequest.getWebRoot(), selectionFacade.loadAllExams());

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffExamJustChanged", Boolean.TRUE);
            httpSession.setAttribute("examSelectMsg", ExamStaffMessage.EXAM_SELECTED.getText());

            String redirect = ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard");
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
            httpSession.setAttribute("examSelectError",
                    ExamStaffMessage.EXAM_CHANGE_ERROR_PREFIX.getText()
                            + (e.getMessage() != null ? e.getMessage() : ExamStaffMessage.UNKNOWN_ERROR.getText()));
            response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard"));
        }
    }

    private void refreshCandidateQueue(HttpSession session, int examId,
            String webRoot, java.util.List<examstaff.dto.ExamSummaryDTO> allExams) {
        if (session == null) {
            return;
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllExams(allExams);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        java.util.List<String> order = (java.util.List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
    }
}
