package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.CandidateQueueHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.CallPageEffects;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.enums.ExamStatus;
import examstaff.enums.ExamStaffMessage;
import examstaff.service.CandidateCallPageService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamControlService;
import examstaff.service.ExamStaffServices;
import examstaff.util.SessionUserHelper;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.view.CallBoardState;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang staff Gọi thí sinh: điều phối HTTP/session ↔ {@link CandidateCallPageService} ↔ CallBoard.
 * <p>
 * Luồng GET (4 bước): build command → preparePage → apply session/board effects → forward JSP.
 * Public Call chỉ đọc board sau khi servlet này sync.
 */
@WebServlet("/views/staff/examstaff/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final CandidateCallPageService pageService;
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final ExamControlService examControlService = SERVICES.examControl();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    public CandidateCallServlet() {
        this(SERVICES.callPage());
    }

    CandidateCallServlet(CandidateCallPageService pageService) {
        this.pageService = pageService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if ("desk".equals(request.getParameter("view"))) {
            redirectToDesk(request, response, session);
            return;
        }

        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, webRoot);

        // 1) Command từ HTTP/session
        CandidateCallPageCommand command = buildCommand(request, session, pageCtx, webRoot);
        // 2) Nghiệp vụ trang
        CandidateCallPageViewDTO view = pageService.preparePage(command);

        if (handleEarlyExit(request, response, session, pageCtx, view)) {
            return;
        }

        // 3) Side-effect session + CallBoard (một chỗ)
        CallPageEffects effects = CallPageEffects.fromView(view);
        applySessionAndBoardEffects(session, pageCtx.getExamId(), view, effects);

        // 4) Bind + forward
        bindCandidateCallPageAttributes(request, session, effects.getPublishExamId(), view.getFullQueue());
        publishCandidateQueue(request, session, view.getFullQueue(), effects.getPublishExamId());
        bindActionAlert(request, view);
        ExamStaffHttpSupport.consumeFlash(session, ExamStaffSessionKeys.EXAM_CONTROL_ERROR,
                request, "examLockedMsg");
        ExamStaffHttpSupport.consumeFlash(session, ExamStaffSessionKeys.EXAM_CONTROL_MSG,
                request, "examControlMsg");

        if (view.isShowSuspended()) {
            request.setAttribute("suspendedList", view.getSuspendedList());
            request.getRequestDispatcher("/views/staff/examstaff/candidate-suspended.jsp")
                    .forward(request, response);
            return;
        }

        request.setAttribute("nextCallingCandidate", view.getNextCallingCandidate());
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private void redirectToDesk(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws IOException {
        String deskSbd = request.getParameter("sbd");
        if (deskSbd == null || deskSbd.trim().isEmpty()) {
            deskSbd = (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
        }
        if (deskSbd != null && !deskSbd.trim().isEmpty()) {
            response.sendRedirect("procedure?sbd=" + deskSbd);
        } else {
            response.sendRedirect("procedure");
        }
    }

    /** Resume shift / redirect do service; trả true nếu đã gửi response. */
    private boolean handleEarlyExit(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamStaffPageFacade.ExamStaffPageContext pageCtx,
            CandidateCallPageViewDTO view) throws IOException {
        if (view.isResumeShift()) {
            ExamSummaryDTO currentExam = selectionFacade.findExamById(
                    selectionFacade.loadAllExams(), pageCtx.getExamId());
            if (currentExam != null && ExamStatus.isPaused(currentExam.getStatus())) {
                ExamControlService.ResumeResult resume = examControlService.resumeExam(pageCtx.getExamId());
                if (!resume.isSuccess()) {
                    session.setAttribute(ExamStaffSessionKeys.EXAM_CONTROL_ERROR, resume.getMessage());
                    response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
                    return true;
                }
                session.setAttribute(ExamStaffSessionKeys.EXAM_CONTROL_MSG, resume.getMessage());
            }
            session.removeAttribute(ExamStaffSessionKeys.SHIFT_ENDED);
            session.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
            callBoardHttp.resumeShift(getServletContext(), pageCtx.getExamId());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return true;
        }
        if (view.getRedirectPath() != null) {
            response.sendRedirect(request.getContextPath() + view.getRedirectPath());
            return true;
        }
        return false;
    }

    private CandidateCallPageCommand buildCommand(HttpServletRequest request, HttpSession session,
            ExamStaffPageFacade.ExamStaffPageContext pageCtx, String webRoot) {
        CandidateCallPageCommand command = new CandidateCallPageCommand();
        command.setAction(request.getParameter("action"));
        command.setSbd(request.getParameter("sbd"));
        command.setView(request.getParameter("view"));
        command.setReturnView(request.getParameter("returnView"));
        command.setExamId(pageCtx.getExamId());
        command.setBoardExamId(pageCtx.getExamId());
        command.setCalledByStaffId(SessionUserHelper.resolveUserId(session));
        command.setWebRoot(webRoot);
        command.setShiftEnded(isShiftEnded(session));
        CallBoardState board = callBoardHttp.getState(getServletContext(), pageCtx.getExamId());
        command.setBoard(board);
        command.setShiftPaused(resolveShiftPaused(session, pageCtx.getExamId(), board));

        ExamSummaryDTO exam = selectionFacade.findExamById(pageCtx.getAllExams(), pageCtx.getExamId());
        boolean mutationsLocked = exam != null && ExamStatus.isLockedForStaffMutation(exam.getStatus());
        command.setExamMutationsLocked(mutationsLocked);
        if (mutationsLocked && isStaffMutationAction(command.getAction())) {
            session.setAttribute(ExamStaffSessionKeys.EXAM_CONTROL_ERROR,
                    ExamStaffMessage.EXAM_MUTATIONS_LOCKED.getText());
            command.setAction(null);
        }

        command.setCallingSbd((String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD));
        command.setLastLoadedExamId((Integer) session.getAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID));
        @SuppressWarnings("unchecked")
        List<String> callQueueOrder =
                (List<String>) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
        command.setCallQueueOrder(callQueueOrder);
        command.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> permanentAbsents =
                (List<ExamRegistrationDTO>) session.getAttribute(ExamStaffSessionKeys.PERMANENT_ABSENTS);
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute(ExamStaffSessionKeys.PERMANENT_ABSENTS, permanentAbsents);
        }
        command.setPermanentAbsents(permanentAbsents);

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> cached =
                (List<ExamRegistrationDTO>) session.getAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE);
        command.setCachedQueue(cached);

        return command;
    }

    private boolean resolveShiftPaused(HttpSession session, int examId, CallBoardState board) {
        if (board != null && board.isExamPaused()) {
            if (session != null) {
                session.setAttribute(ExamStaffSessionKeys.SHIFT_PAUSED, ExamStaffSessionKeys.FLAG_TRUE);
            }
            return true;
        }
        ExamSummaryDTO exam = selectionFacade.findExamById(selectionFacade.loadAllExams(), examId);
        if (exam != null && ExamStatus.isPaused(exam.getStatus())) {
            if (session != null) {
                session.setAttribute(ExamStaffSessionKeys.SHIFT_PAUSED, ExamStaffSessionKeys.FLAG_TRUE);
            }
            return true;
        }
        return isShiftPaused(session);
    }

    /** Áp toàn bộ side-effect session + CallBoard từ {@link CallPageEffects}. */
    private void applySessionAndBoardEffects(HttpSession session, int boardExamId,
            CandidateCallPageViewDTO view, CallPageEffects effects) {
        if (effects.isClearCallingSbd()) {
            session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
        } else if (effects.getCallingSbd() != null) {
            session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, effects.getCallingSbd());
        }
        if (effects.isShiftEnded()) {
            session.setAttribute(ExamStaffSessionKeys.SHIFT_ENDED, ExamStaffSessionKeys.FLAG_TRUE);
            session.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        }
        if (effects.isShiftPaused()) {
            session.setAttribute(ExamStaffSessionKeys.SHIFT_PAUSED, ExamStaffSessionKeys.FLAG_TRUE);
        } else if (view.isResumeShift() || effects.isShiftEnded()) {
            session.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        }
        if (effects.isClearProcedureJustPaidSbd()) {
            session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID_SBD);
        }
        if (effects.isPersistQueueOrder()) {
            ExamStaffPageBinder.syncCallQueueOrder(
                    session, effects.getPublishExamId(), view.getFullQueue());
        }

        if (effects.isPauseBoard()) {
            examControlService.pauseExam(boardExamId);
            callBoardHttp.pauseShift(getServletContext(), boardExamId, view.getFullQueue());
        }
        if (effects.isReleaseDesk()) {
            callBoardHttp.releaseDeskAndCall(
                    getServletContext(), boardExamId, effects.getReleaseDeskCallingSbd(),
                    view.getFullQueue(), effects.isShiftEnded());
        }
        if (effects.isSyncBoard()) {
            callBoardHttp.sync(
                    getServletContext(), boardExamId, effects.getBoardCallingSbd(),
                    view.getFullQueue(), effects.isShiftEnded());
        }
    }

    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> queue) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, queue);
        int resolvedExamId = selectionFacade.resolveExamId(request, session, null, 0);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        ExamSummaryDTO current = selectionFacade.findExamById(
                selectionFacade.loadAllExams(), resolvedExamId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeExam(
                    selectionFacade.loadAllExams(), examId);
        }
        int suspendedCount = candidateQueueService.listSuspendedInExam(queue).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> queue) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
        ExamRegistrationDTO calling = candidateQueueService.resolveCallingCandidate(callingSbd, queue);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
        }
        return calling;
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> queue, int examId) {
        CandidateQueueHttpSupport.publishLists(request, session, candidateQueueService,
                selectionFacade, queue, examId);
    }

    private static void bindActionAlert(HttpServletRequest request, CandidateCallPageViewDTO view) {
        if (view.getAlertType() == CandidateCallActionResultDTO.AlertType.NONE
                || view.getAlertSbd() == null) {
            return;
        }
        switch (view.getAlertType()) {
            case AUTO_ABSENT -> request.setAttribute("autoAbsentAlert", view.getAlertSbd());
            case ABSENT -> request.setAttribute("absentAlert", view.getAlertSbd());
            case PERMANENT_ABSENT -> request.setAttribute("permanentAbsentAlert", view.getAlertSbd());
            case UNDO -> request.setAttribute("undoAlert", view.getAlertSbd());
            default -> {
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static boolean isShiftEnded(HttpSession session) {
        return session != null
                && ExamStaffSessionKeys.FLAG_TRUE.equals(session.getAttribute(ExamStaffSessionKeys.SHIFT_ENDED));
    }

    private static boolean isShiftPaused(HttpSession session) {
        return session != null
                && ExamStaffSessionKeys.FLAG_TRUE.equals(session.getAttribute(ExamStaffSessionKeys.SHIFT_PAUSED));
    }

    /** Action thay đổi hồ sơ/đình chỉ khi kỳ đã kết thúc. */
    private static boolean isStaffMutationAction(String action) {
        if (action == null || action.isBlank()) {
            return false;
        }
        return switch (action) {
            case "permanentAbsent", "undoAbsent", "absent", "moveToBottom", "autoAbsent",
                    "startCall", "endShift", "closeExam", "pauseShift" -> true;
            default -> false;
        };
    }
}
