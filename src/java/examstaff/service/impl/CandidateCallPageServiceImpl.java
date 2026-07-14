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
import examstaff.service.CandidateCallingService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffSessionQueryService;

import java.util.ArrayList;
import java.util.List;

public class CandidateCallPageServiceImpl implements CandidateCallPageService {

    private final CandidateCallWorkflowService callWorkflow;
    private final CandidateCallingService callingService;
    private final CandidateQueueService queueService;
    private final ExamStaffSessionQueryService sessionQuery;

    public CandidateCallPageServiceImpl() {
        this(new CandidateCallWorkflowServiceImpl(), new CandidateCallingServiceImpl(),
                new CandidateQueueServiceImpl(), new ExamStaffSessionQueryServiceImpl());
    }

    public CandidateCallPageServiceImpl(CandidateCallWorkflowService callWorkflow,
            CandidateCallingService callingService,
            CandidateQueueService queueService,
            ExamStaffSessionQueryService sessionQuery) {
        this.callWorkflow = callWorkflow;
        this.callingService = callingService;
        this.queueService = queueService;
        this.sessionQuery = sessionQuery;
    }

    @Override
    public CandidateCallPageViewDTO preparePage(CandidateCallPageCommand command) {
        CandidateCallPageViewDTO view = new CandidateCallPageViewDTO();
        if (command == null) {
            return view;
        }

        int examId = command.getExamId();
        int boardExamId = command.getBoardExamId();
        boolean shiftEnded = command.isShiftEnded();
        String callingSbd = command.getCallingSbd();
        List<ExamRegistrationDTO> permanentAbsents = command.getPermanentAbsents();
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
        }

        String action = command.getAction();
        if ("startShift".equals(action)) {
            view.setResumeShift(true);
            view.setRedirectPath("/views/staff/examstaff/candidatecall");
            return view;
        }

        boolean shiftPaused = command.isShiftPaused();
        List<ExamRegistrationDTO> fullQueue = loadFullQueue(command, examId, shiftEnded, shiftPaused);

        if (action != null && shiftPaused && "startCall".equals(action)) {
            action = null;
        }

        if (action != null) {
            CandidateCallActionResultDTO actionResult = callWorkflow.executeAction(
                    action, command.getSbd(), fullQueue, permanentAbsents, boardExamId,
                    shiftEnded, command.getCalledByStaffId());

            if (actionResult.isRedirectToCallPage()) {
                view.setRedirectPath("/views/staff/examstaff/candidatecall");
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

        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        String advancedSbd = callingService.advanceCallingIfDone(callingSbd, fullQueue);
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
            String synced = callingService.resolveSyncedCallingSbd(callingSbd, command.getBoard(), fullQueue);
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
            view.setSuspendedList(queueService.listSuspendedInSession(fullQueue));
        }

        String nextSbd = queueService.resolveNextCallingSbd(fullQueue, callingSbd);
        view.setNextCallingCandidate(queueService.findBySbd(activeQueue, nextSbd));
        return view;
    }

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
        refresh.setAllSessions(sessionQuery.listAllSessions());
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

    private String promoteCaller(CandidateCallPageViewDTO view, List<ExamRegistrationDTO> activeQueue,
            String nextSbd, int calledByStaffId, String currentCallingSbd) {
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

    private DeskRelease releaseDeskIfProcedureDone(CallBoardState board, List<ExamRegistrationDTO> fullQueue,
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

    private static final class DeskRelease {
        boolean applied;
        String callingSbd;
        String boardCallingSbd;
    }
}

