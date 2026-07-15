package examstaff.service.impl.support.call;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.CallBoardState;
import java.util.ArrayList;
import java.util.List;

/**
 * Dựng ViewModel trang gọi thí sinh: load hàng đợi, chạy action workflow,
 * advance/sync callingSbd với CallBoard, chuẩn bị alert và danh sách suspended.
 */
public class CandidateCallPageServiceImpl {

    private final CandidateCallWorkflowServiceImpl callWorkflow;
    private final CandidateQueueServiceImpl queueService;
    private final ExamStaffExamQueryServiceImpl examQuery;

    /** Wiring mặc định khi không inject từ composition root. */
    public CandidateCallPageServiceImpl() {
        this(new CandidateCallWorkflowServiceImpl(), new CandidateQueueServiceImpl(),
                new ExamStaffExamQueryServiceImpl());
    }

    /**
     * Inject dependencies cho unit test / composition root.
     *
     * @param callWorkflow workflow action gọi / vắng / đóng ca
     * @param queueService thao tác hàng đợi và resolve SBD
     * @param examQuery    danh sách kỳ thi phục vụ refresh queue
     */
    public CandidateCallPageServiceImpl(CandidateCallWorkflowServiceImpl callWorkflow,
            CandidateQueueServiceImpl queueService,
            ExamStaffExamQueryServiceImpl examQuery) {
        this.callWorkflow = callWorkflow;
        this.queueService = queueService;
        this.examQuery = examQuery;
    }

    /**
     * Chuẩn bị view trang gọi từ command (session + request).
     * Luồng: validate → load queue → chạy action → advance/sync board → bind view.
     *
     * @param command ngữ cảnh trang (examId, action, callingSbd, board, …)
     * @return DTO view cho consolidator/servlet bind session + JSP
     */
    public CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command) {
        CandidateCallPageViewDTO view = new CandidateCallPageViewDTO();
        // Validate
        if (command == null) {
            return view;
        }

        // Load ngữ cảnh từ command
        int examId = command.getExamId();
        int boardExamId = command.getBoardExamId();
        boolean shiftEnded = command.isShiftEnded();
        String callingSbd = command.getCallingSbd();
        List<ExamRegistrationDTO> permanentAbsents = command.getPermanentAbsents();
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
        }

        // Nhánh resume ca → redirect sớm
        String action = command.getAction();
        if ("startShift".equals(action)) {
            view.setResumeShift(true);
            view.setRedirectPath("/examstaff/candidatecall");
            return view;
        }

        boolean shiftPaused = command.isShiftPaused();
        List<ExamRegistrationDTO> fullQueue = loadFullQueue(command, examId, shiftEnded, shiftPaused);

        // Khi ca paused: bỏ qua startCall (không promote)
        if (action != null && shiftPaused && "startCall".equals(action)) {
            action = null;
        }

        // Mutate qua workflow nếu có action
        if (action != null) {
            CandidateCallActionResultDTO actionResult = callWorkflow.executeAction(
                    action, command.getSbd(), fullQueue, permanentAbsents, boardExamId,
                    shiftEnded, command.getCalledByStaffId());

            if (actionResult.isRedirectToCallPage()) {
                view.setRedirectPath("/examstaff/candidatecall");
                return view;
            }

            applyActionResult(view, actionResult);
            shiftEnded = actionResult.isShiftEnded() || shiftEnded;
            shiftPaused = actionResult.isShiftPaused() || shiftPaused;
            if (actionResult.isClearCallingSbd()) {
                callingSbd = null;
            } else if (actionResult.getCallingSbd() != null) {
                callingSbd = actionResult.getCallingSbd();
            }

            if (actionResult.isReloadQueue()) {
                command.setShiftEnded(shiftEnded);
                command.setShiftPaused(shiftPaused);
                fullQueue = loadFullQueue(command, examId, shiftEnded, shiftPaused);
            }

            if (actionResult.isSyncQueueOrder()) {
                view.setPersistQueueOrder(true);
            }

            if (actionResult.isMoveRestoredToFront() && command.getSbd() != null) {
                ExamRegistrationDTO fresh = queueService.findBySbd(fullQueue, command.getSbd());
                if (fresh != null) {
                    queueService.moveCallableCandidateToFront(fullQueue, command.getSbd());
                    view.setPersistQueueOrder(true);
                }
            }

            if (actionResult.getPromoteAfterSbd() != null) {
                List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
                String nextSbd = queueService.resolveNextCallingSbd(fullQueue, actionResult.getPromoteAfterSbd());
                callingSbd = promoteCaller(view, activeQueue, nextSbd, command.getCalledByStaffId(), callingSbd);
            }

            if (actionResult.isSyncQueueOrder()) {
                view.setPersistQueueOrder(true);
            }

            view.setAlertType(actionResult.getAlertType());
            view.setAlertSbd(actionResult.getAlertSbd());
        }

        // Advance calling nếu thí sinh hiện tại đã xong / giải phóng bàn
        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        String advancedSbd = queueService.advanceCallingIfDone(callingSbd, fullQueue);
        boolean releaseDesk = false;
        String releaseDeskCallingSbd = null;

        if (!shiftPaused && advancedSbd != null && !advancedSbd.isBlank()) {
            if (callingSbd != null && !advancedSbd.equals(callingSbd)) {
                callingSbd = advancedSbd;
                activeQueue = queueService.filterPendingForCall(fullQueue);
                callingSbd = promoteCaller(view, activeQueue, advancedSbd, command.getCalledByStaffId(), callingSbd);
                releaseDesk = true;
                releaseDeskCallingSbd = advancedSbd;
                view.setClearProcedureJustPaidSbd(true);
            }
        } else if (!shiftPaused && callingSbd != null) {
            callingSbd = null;
            view.setClearCallingSbd(true);
        } else {
            DeskRelease release = releaseDeskIfProcedureDone(command.getBoard(), fullQueue, callingSbd);
            if (release.applied) {
                callingSbd = release.callingSbd;
                releaseDesk = true;
                releaseDeskCallingSbd = release.boardCallingSbd;
                view.setClearProcedureJustPaidSbd(true);
                if (callingSbd == null) {
                    view.setClearCallingSbd(true);
                }
            }
        }

        // Sync board calling khi ca còn mở / đã đóng
        activeQueue = queueService.filterPendingForCall(fullQueue);
        boolean syncBoard = false;
        String boardCallingSbd = callingSbd;

        if (!shiftEnded && !shiftPaused) {
            DeskRelease release = releaseDeskIfProcedureDone(command.getBoard(), fullQueue, callingSbd);
            if (release.applied) {
                callingSbd = release.callingSbd;
                view.setClearProcedureJustPaidSbd(true);
                if (callingSbd == null) {
                    view.setClearCallingSbd(true);
                }
                releaseDesk = true;
                releaseDeskCallingSbd = release.boardCallingSbd;
            }
            String synced = queueService.resolveSyncedCallingSbd(callingSbd, command.getBoard(), fullQueue);
            if (synced != null) {
                callingSbd = synced;
            }
            // Giống ExamStaffViewHelper.syncCallingSbd: luôn sync board khi ca chưa kết thúc.
            syncBoard = true;
            boardCallingSbd = callingSbd;
        } else if (shiftEnded) {
            syncBoard = true;
            boardCallingSbd = null;
        }

        // Result: bind view
        view.setFullQueue(fullQueue);
        view.setActiveQueue(activeQueue);
        if (callingSbd == null || callingSbd.isBlank()) {
            view.setCallingSbd(null);
            view.setClearCallingSbd(true);
        } else {
            view.setCallingSbd(callingSbd);
            view.setClearCallingSbd(false);
        }
        view.setShiftEnded(shiftEnded);
        view.setShiftPaused(shiftPaused);
        if (shiftPaused && "pauseShift".equals(action)) {
            view.setPauseBoard(true);
        }
        view.setReleaseDesk(releaseDesk);
        view.setReleaseDeskCallingSbd(releaseDeskCallingSbd);
        view.setSyncBoard(syncBoard);
        view.setBoardCallingSbd(boardCallingSbd);
        view.setPublishExamId(boardExamId > 0 ? boardExamId : examId);

        boolean showSuspended = "suspended".equals(command.getView())
                || "suspended".equals(command.getReturnView());
        view.setShowSuspended(showSuspended);
        if (showSuspended) {
            view.setSuspendedList(queueService.listSuspendedInExam(fullQueue));
        }

        String nextSbd = queueService.resolveNextCallingSbd(fullQueue, callingSbd);
        view.setNextCallingCandidate(queueService.findBySbd(activeQueue, nextSbd));
        return view;
    }

    /**
     * Tải hàng đợi đầy đủ: dùng cache session khi ca paused/ended, ngược lại refresh từ DB.
     *
     * @param command    command trang (cache + callQueueOrder)
     * @param examId     mã kỳ đang chọn
     * @param shiftEnded ca đã đóng
     * @param shiftPaused ca đang tạm dừng
     * @return bản sao / danh sách hàng đợi (không null)
     */
    private List<ExamRegistrationDTO> loadFullQueue(CandidateCallPageCommand command, int examId,
            boolean shiftEnded, boolean shiftPaused) {
        // Validate: ưu tiên cache khi ca đóng/paused
        if (shiftEnded || shiftPaused) {
            Integer lastLoadedExam = command.getLastLoadedExamId();
            if (lastLoadedExam != null && lastLoadedExam == examId
                    && command.getCachedQueue() != null && !command.getCachedQueue().isEmpty()) {
                return new ArrayList<>(command.getCachedQueue());
            }
        }

        // Load từ DAO qua refreshQueue
        ExamStaffPageCommand refresh = new ExamStaffPageCommand();
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

    /**
     * Áp side-effect calling/shift từ kết quả workflow lên view.
     *
     * @param view   view đang dựng
     * @param result kết quả action
     */
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
     * Promote SBD lên số đang gọi: ghi audit Calling và cập nhật view.
     *
     * @param view              view đang dựng
     * @param activeQueue       hàng pending
     * @param nextSbd           SBD promote (có thể blank → clear)
     * @param calledByStaffId   userId staff
     * @param currentCallingSbd SBD đang gọi trước đó
     * @return SBD đang gọi sau promote (null nếu clear)
     */
    private String promoteCaller(CandidateCallPageViewDTO view, List<ExamRegistrationDTO> activeQueue,
            String nextSbd, int calledByStaffId, String currentCallingSbd) {
        // Mutate / Result
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

    /**
     * Nếu bàn đang bận và thí sinh tại bàn đã xong thủ tục → đề xuất giải phóng & gọi tiếp.
     *
     * @param board      trạng thái CallBoard (có thể null)
     * @param fullQueue  hàng đợi đầy đủ
     * @param callingSbd SBD đang gọi HTTP (có thể null)
     * @return gói DeskRelease (applied=false nếu không áp dụng)
     */
    private DeskRelease releaseDeskIfProcedureDone(CallBoardState board, List<ExamRegistrationDTO> fullQueue,
            String callingSbd) {
        DeskRelease release = new DeskRelease();
        // Validate: bàn phải bận và có deskSbd
        if (board == null || !board.isDeskBusy() || board.getDeskSbd() == null || board.getDeskSbd().isBlank()) {
            return release;
        }
        ExamRegistrationDTO atDesk = queueService.findBySbd(fullQueue, board.getDeskSbd());
        if (atDesk == null || !atDesk.isProcedureComplete()) {
            return release;
        }
        // Resolve SBD kế tiếp để gọi sau khi giải phóng
        String nextSbd = callingSbd;
        if (nextSbd == null || nextSbd.isBlank() || nextSbd.equals(board.getDeskSbd())) {
            nextSbd = queueService.resolveNextCallingSbd(fullQueue, board.getDeskSbd());
        }
        release.applied = true;
        release.callingSbd = nextSbd;
        release.boardCallingSbd = nextSbd;
        return release;
    }

    /** Kết quả nội bộ: có giải phóng bàn và SBD gọi tiếp theo. */
    private static final class DeskRelease {
        boolean applied;
        String callingSbd;
        String boardCallingSbd;
    }
}
