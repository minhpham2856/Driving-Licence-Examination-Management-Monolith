package service.impl;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateCallActionResultDTO;
import dto.examstaff.CandidateCallPageCommand;
import dto.examstaff.CandidateCallPageViewDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffQueueRefreshInput;
import model.view.CallBoardState;
import service.CandidateCallPageService;
import service.CandidateCallWorkflowService;
import service.CandidateCallingService;
import service.CandidateQueueService;
import service.ExamStaffSessionQueryService;

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
        int boardSessionId = command.getBoardSessionId();
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

        List<ExamRegistrationDTO> fullQueue = loadFullQueue(command, examId, shiftEnded);

        if (action != null) {
            CandidateCallActionResultDTO actionResult = callWorkflow.executeAction(
                    action, command.getSbd(), fullQueue, permanentAbsents, boardSessionId,
                    shiftEnded, command.getCalledByStaffId());

            if (actionResult.isRedirectToCallPage()) {
                view.setRedirectPath("/views/staff/examstaff/candidatecall");
                return view;
            }

            applyActionResult(view, actionResult);
            shiftEnded = actionResult.isShiftEnded() || shiftEnded;
            if (actionResult.isClearCallingSbd()) {
                callingSbd = null;
            } else if (actionResult.getCallingSbd() != null) {
                callingSbd = actionResult.getCallingSbd();
            }

            if (actionResult.isReloadQueue()) {
                command.setShiftEnded(shiftEnded);
                fullQueue = loadFullQueue(command, examId, shiftEnded);
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

        if (advancedSbd != null && !advancedSbd.isBlank()) {
            if (callingSbd != null && !advancedSbd.equals(callingSbd)) {
                callingSbd = advancedSbd;
                activeQueue = queueService.filterPendingForCall(fullQueue);
                callingSbd = promoteCaller(view, activeQueue, advancedSbd, command.getCalledByStaffId(), callingSbd);
                releaseDesk = true;
                releaseDeskCallingSbd = advancedSbd;
                view.setClearProcedureJustPaidSbd(true);
            }
        } else if (callingSbd != null) {
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

        if (!shiftEnded) {
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
        view.setReleaseDesk(releaseDesk);
        view.setReleaseDeskCallingSbd(releaseDeskCallingSbd);
        view.setSyncBoard(syncBoard);
        view.setBoardCallingSbd(boardCallingSbd);
        view.setPublishExamId(examId);
        view.setPublishSessionId(boardSessionId);

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
            boolean shiftEnded) {
        if (shiftEnded) {
            Integer lastLoadedExam = command.getLastLoadedExamId();
            if (lastLoadedExam != null && lastLoadedExam == examId
                    && command.getCachedQueue() != null && !command.getCachedQueue().isEmpty()) {
                return new ArrayList<>(command.getCachedQueue());
            }
        }

        ExamStaffQueueRefreshInput refresh = new ExamStaffQueueRefreshInput();
        refresh.setExamId(examId);
        int sessionId = command.getBoardSessionId();
        refresh.setSessionId(sessionId);
        if (sessionId > 0) {
            refresh.setSelectedSessionId(sessionId);
        }
        refresh.setWebRoot(command.getWebRoot());
        refresh.setAllSessions(sessionQuery.listAllSessions());
        refresh.setCallQueueOrder(command.getCallQueueOrder());
        refresh.setCallQueueOrderSessionId(command.getCallQueueOrderSessionId());
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
