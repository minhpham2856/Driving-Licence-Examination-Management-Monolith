package examstaff.service.impl;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CandidateCallPageService;
import examstaff.service.CandidateCallWorkflowService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffExamQueryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator trang Gọi thí sinh.
 * <p>
 * {@link #preparePage} theo 4 bước (slide defense):
 * <ol>
 *   <li>{@code applyCallAction} — chạy action (gọi/vắng/pause…)</li>
 *   <li>{@code reconcileCallingAfterAdvance} — nhảy SBD nếu thí sinh hiện tại đã xong</li>
 *   <li>{@code releaseDeskWhenProcedureDone} — giải phóng bàn khi desk đã hoàn tất thủ tục</li>
 *   <li>{@code decideBoardAndSessionEffects} — quyết định sync board + cờ session</li>
 * </ol>
 */
public class CandidateCallPageServiceImpl implements CandidateCallPageService {

    private final CandidateCallWorkflowService callWorkflow;
    private final CandidateQueueService queueService;
    private final ExamStaffExamQueryService examQuery;

    /** Wiring mặc định. */
    public CandidateCallPageServiceImpl() {
        this(new CandidateCallWorkflowServiceImpl(),
                new CandidateQueueServiceImpl(), new ExamStaffExamQueryServiceImpl());
    }

    /**
     * @param callWorkflow thực thi action gọi thí sinh
     * @param queueService refresh / lọc / sắp hàng đợi / advance SBD
     * @param examQuery    danh sách kỳ thi khi refresh queue
     */
    public CandidateCallPageServiceImpl(CandidateCallWorkflowService callWorkflow,
            CandidateQueueService queueService,
            ExamStaffExamQueryService examQuery) {
        this.callWorkflow = callWorkflow;
        this.queueService = queueService;
        this.examQuery = examQuery;
    }

    /** {@inheritDoc} */
    @Override
    public CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command) {
        CandidateCallPageViewDTO view = new CandidateCallPageViewDTO();
        if (command == null) {
            return view;
        }

        if ("startShift".equals(command.getAction())) {
            view.setResumeShift(true);
            view.setRedirectPath("/views/staff/examstaff/candidatecall");
            return view;
        }

        PageState state = new PageState(command);
        state.fullQueue = loadFullQueue(command, state.examId, state.shiftEnded, state.shiftPaused);

        if (state.action != null && state.shiftPaused && "startCall".equals(state.action)) {
            state.action = null;
        }

        if (state.action != null && command.isExamMutationsLocked()
                && isBlockedWhenExamLocked(state.action)) {
            state.action = null;
        }

        // 1) Action nghiệp vụ
        if (state.action != null) {
            if (applyCallAction(view, command, state)) {
                return view;
            }
        }

        // 2) Advance calling nếu SBD hiện tại đã xong
        reconcileCallingAfterAdvance(view, command, state);

        // 3+4) Desk release (lần 2 khi ca đang chạy) + quyết định board/session
        decideBoardAndSessionEffects(view, command, state);

        bindQueueAndNextCaller(view, command, state);
        return view;
    }

    /**
     * Bước 1: chạy workflow action; trả {@code true} nếu cần redirect sớm.
     */
    private boolean applyCallAction(CandidateCallPageViewDTO view, CandidateCallPageCommand command,
            PageState state) {
        CandidateCallActionResultDTO actionResult = callWorkflow.executeAction(
                state.action, command.getSbd(), state.fullQueue, state.permanentAbsents, state.boardExamId,
                state.shiftEnded, command.getCalledByStaffId());

        if (actionResult.isRedirectToCallPage()) {
            view.setRedirectPath("/views/staff/examstaff/candidatecall");
            return true;
        }

        applyActionResult(view, actionResult);
        state.shiftEnded = actionResult.isShiftEnded() || state.shiftEnded;
        state.shiftPaused = actionResult.isShiftPaused() || state.shiftPaused;
        if (actionResult.isClearCallingSbd()) {
            state.callingSbd = null;
        } else if (actionResult.getCallingSbd() != null) {
            state.callingSbd = actionResult.getCallingSbd();
        }

        if (actionResult.isReloadQueue()) {
            command.setShiftEnded(state.shiftEnded);
            command.setShiftPaused(state.shiftPaused);
            state.fullQueue = loadFullQueue(command, state.examId, state.shiftEnded, state.shiftPaused);
        }

        if (actionResult.isSyncQueueOrder()) {
            view.setPersistQueueOrder(true);
        }

        if (actionResult.isMoveRestoredToFront() && command.getSbd() != null) {
            ExamRegistrationDTO fresh = queueService.findBySbd(state.fullQueue, command.getSbd());
            if (fresh != null) {
                queueService.moveCallableCandidateToFront(state.fullQueue, command.getSbd());
                view.setPersistQueueOrder(true);
            }
        }

        if (actionResult.getPromoteAfterSbd() != null) {
            List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(state.fullQueue);
            String nextSbd = queueService.resolveNextCallingSbd(
                    state.fullQueue, actionResult.getPromoteAfterSbd());
            state.callingSbd = assignNextCallerAndAudit(
                    view, activeQueue, nextSbd, command.getCalledByStaffId());
        }

        if (actionResult.isSyncQueueOrder()) {
            view.setPersistQueueOrder(true);
        }

        view.setAlertType(actionResult.getAlertType());
        view.setAlertSbd(actionResult.getAlertSbd());
        return false;
    }

    /**
     * Bước 2: sau advance — promote SBD mới / clear calling / release desk lần 1.
     */
    private void reconcileCallingAfterAdvance(CandidateCallPageViewDTO view,
            CandidateCallPageCommand command, PageState state) {
        String advancedSbd = queueService.advanceCallingIfDone(state.callingSbd, state.fullQueue);

        if (!state.shiftPaused && advancedSbd != null && !advancedSbd.isBlank()) {
            if (state.callingSbd != null && !advancedSbd.equals(state.callingSbd)) {
                state.callingSbd = advancedSbd;
                List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(state.fullQueue);
                state.callingSbd = assignNextCallerAndAudit(
                        view, activeQueue, advancedSbd, command.getCalledByStaffId());
                state.releaseDesk = true;
                state.releaseDeskCallingSbd = advancedSbd;
                view.setClearProcedureJustPaidSbd(true);
            }
        } else if (!state.shiftPaused && state.callingSbd != null) {
            // advance trả empty ⇒ clear calling (giữ hành vi cũ)
            state.callingSbd = null;
            view.setClearCallingSbd(true);
        } else {
            releaseDeskWhenProcedureDone(view, command.getBoard(), state);
        }
    }

    /**
     * Bước 3: nếu người ở bàn đã xong thủ tục → release desk + chọn SBD gọi tiếp.
     */
    private void releaseDeskWhenProcedureDone(CandidateCallPageViewDTO view, CallBoardState board,
            PageState state) {
        DeskRelease release = computeDeskRelease(board, state.fullQueue, state.callingSbd);
        if (!release.applied) {
            return;
        }
        state.callingSbd = release.callingSbd;
        state.releaseDesk = true;
        state.releaseDeskCallingSbd = release.boardCallingSbd;
        view.setClearProcedureJustPaidSbd(true);
        if (state.callingSbd == null) {
            view.setClearCallingSbd(true);
        }
    }

    /**
     * Bước 4: quyết định sync CallBoard + gắn cờ session lên ViewDTO.
     */
    private void decideBoardAndSessionEffects(CandidateCallPageViewDTO view,
            CandidateCallPageCommand command, PageState state) {
        if (!state.shiftEnded && !state.shiftPaused) {
            releaseDeskWhenProcedureDone(view, command.getBoard(), state);
            String synced = queueService.resolveSyncedCallingSbd(
                    state.callingSbd, command.getBoard(), state.fullQueue);
            if (synced != null) {
                state.callingSbd = synced;
            }
            state.syncBoard = true;
            state.boardCallingSbd = state.callingSbd;
        } else if (state.shiftEnded) {
            state.syncBoard = true;
            state.boardCallingSbd = null;
        }

        if (state.callingSbd == null || state.callingSbd.isBlank()) {
            view.setCallingSbd(null);
            view.setClearCallingSbd(true);
        } else {
            view.setCallingSbd(state.callingSbd);
            view.setClearCallingSbd(false);
        }
        view.setShiftEnded(state.shiftEnded);
        view.setShiftPaused(state.shiftPaused);
        if (state.shiftPaused && "pauseShift".equals(state.action)) {
            view.setPauseBoard(true);
        }
        view.setReleaseDesk(state.releaseDesk);
        view.setReleaseDeskCallingSbd(state.releaseDeskCallingSbd);
        view.setSyncBoard(state.syncBoard);
        view.setBoardCallingSbd(state.boardCallingSbd);
        view.setPublishExamId(state.boardExamId > 0 ? state.boardExamId : state.examId);
    }

    /** Gắn queue/active/suspended/next caller lên view. */
    private void bindQueueAndNextCaller(CandidateCallPageViewDTO view,
            CandidateCallPageCommand command, PageState state) {
        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(state.fullQueue);
        view.setFullQueue(state.fullQueue);
        view.setActiveQueue(activeQueue);

        boolean showSuspended = "suspended".equals(command.getView())
                || "suspended".equals(command.getReturnView());
        view.setShowSuspended(showSuspended);
        if (showSuspended) {
            view.setSuspendedList(queueService.listSuspendedInExam(state.fullQueue));
        }

        String nextSbd = queueService.resolveNextCallingSbd(state.fullQueue, state.callingSbd);
        view.setNextCallingCandidate(queueService.findBySbd(activeQueue, nextSbd));
    }

    /**
     * Load hàng đợi: khi ca pause/end dùng cache session nếu cùng exam; ngược lại refresh từ DB.
     */
    private List<ExamRegistrationDTO> loadFullQueue(CandidateCallPageCommand command, int examId,
            boolean shiftEnded, boolean shiftPaused) {
        if (shiftEnded || shiftPaused) {
            Integer lastLoadedExam = command.getLastLoadedExamId();
            if (lastLoadedExam != null && lastLoadedExam == examId
                    && command.getCachedQueue() != null && !command.getCachedQueue().isEmpty()) {
                return new ArrayList<>(command.getCachedQueue());
            }
        }

        ExamStaffQueueRefreshInput refresh = new ExamStaffQueueRefreshInput();
        refresh.setExamId(examId);
        int boardExamId = command.getBoardExamId();
        if (boardExamId > 0) {
            refresh.setExamId(boardExamId);
            refresh.setSelectedExamId(boardExamId);
        }
        refresh.setWebRoot(command.getWebRoot());
        refresh.setAllExams(examQuery.listAllExams());
        refresh.setCallQueueOrder(command.getCallQueueOrder());
        refresh.setCallQueueOrderExamId(command.getCallQueueOrderExamId());
        CandidateQueueSnapshotDTO snapshot = queueService.refreshQueue(refresh);
        return snapshot.getFullQueue() != null ? snapshot.getFullQueue() : new ArrayList<>();
    }

    private static void applyActionResult(CandidateCallPageViewDTO view, CandidateCallActionResultDTO result) {
        if (result.isClearCallingSbd()) {
            view.setClearCallingSbd(true);
            view.setCallingSbd(null);
        } else if (result.getCallingSbd() != null) {
            view.setCallingSbd(result.getCallingSbd());
        }
        if (result.isShiftEnded()) {
            view.setShiftEnded(true);
        }
        if (result.isShiftPaused()) {
            view.setShiftPaused(true);
        }
    }

    /**
     * Promote SBD lên số đang gọi và ghi audit CALL; clear calling nếu không còn ai.
     *
     * @return SBD đang gọi mới hoặc null
     */
    private String assignNextCallerAndAudit(CandidateCallPageViewDTO view,
            List<ExamRegistrationDTO> activeQueue, String nextSbd, int calledByStaffId) {
        if (nextSbd != null && !nextSbd.isBlank()) {
            callWorkflow.recordCallingCandidate(activeQueue, nextSbd, calledByStaffId);
            view.setCallingSbd(nextSbd);
            view.setClearCallingSbd(false);
            return nextSbd;
        }
        view.setClearCallingSbd(true);
        view.setCallingSbd(null);
        return null;
    }

    /** Thao tác đổi hồ sơ/đình chỉ/hoàn tác — bị chặn khi kỳ đã kết thúc. */
    private static boolean isBlockedWhenExamLocked(String action) {
        if (action == null || action.isBlank()) {
            return false;
        }
        return switch (action) {
            case "permanentAbsent", "undoAbsent", "absent", "moveToBottom", "autoAbsent",
                    "startCall", "endShift", "closeExam", "pauseShift" -> true;
            default -> false;
        };
    }

    private DeskRelease computeDeskRelease(CallBoardState board, List<ExamRegistrationDTO> fullQueue,
            String callingSbd) {
        DeskRelease release = new DeskRelease();
        if (board == null || !board.isDeskBusy() || board.getDeskSbd() == null || board.getDeskSbd().isBlank()) {
            return release;
        }
        ExamRegistrationDTO atDesk = queueService.findBySbd(fullQueue, board.getDeskSbd());
        if (atDesk == null || !atDesk.isProcedureComplete()) {
            return release;
        }
        String nextSbd = callingSbd;
        if (nextSbd == null || nextSbd.isBlank() || nextSbd.equals(board.getDeskSbd())) {
            nextSbd = queueService.resolveNextCallingSbd(fullQueue, board.getDeskSbd());
        }
        release.applied = true;
        release.callingSbd = nextSbd;
        release.boardCallingSbd = nextSbd;
        return release;
    }

    /** Trạng thái mutable xuyên suốt 4 bước preparePage. */
    private static final class PageState {
        final int examId;
        final int boardExamId;
        String action;
        boolean shiftEnded;
        boolean shiftPaused;
        String callingSbd;
        List<ExamRegistrationDTO> permanentAbsents;
        List<ExamRegistrationDTO> fullQueue;
        boolean releaseDesk;
        String releaseDeskCallingSbd;
        boolean syncBoard;
        String boardCallingSbd;

        PageState(CandidateCallPageCommand command) {
            this.examId = command.getExamId();
            this.boardExamId = command.getBoardExamId();
            this.action = command.getAction();
            this.shiftEnded = command.isShiftEnded();
            this.shiftPaused = command.isShiftPaused();
            this.callingSbd = command.getCallingSbd();
            this.permanentAbsents = command.getPermanentAbsents();
            if (this.permanentAbsents == null) {
                this.permanentAbsents = new ArrayList<>();
            }
        }
    }

    private static final class DeskRelease {
        boolean applied;
        String callingSbd;
        String boardCallingSbd;
    }
}
