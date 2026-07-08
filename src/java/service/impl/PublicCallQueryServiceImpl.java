package service.impl;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.PublicCallSnapshotDTO;
import model.view.CallBoardState;
import service.CallBoardSyncService;
import service.CandidateQueueQueryService;
import service.ExamStaffSessionQueryService;
import service.PublicCallQueryService;
import util.examstaff.CallBoardRules;
import util.examstaff.CallQueueRules;

import java.util.ArrayList;
import java.util.List;

public class PublicCallQueryServiceImpl implements PublicCallQueryService {

    private final CandidateQueueQueryService queueQueryService;
    private final ExamStaffSessionQueryService sessionQueryService;
    private final CallBoardSyncService callBoardSyncService;

    public PublicCallQueryServiceImpl() {
        this(new CandidateQueueQueryServiceImpl(), new ExamStaffSessionQueryServiceImpl(),
                new CallBoardSyncServiceImpl());
    }

    public PublicCallQueryServiceImpl(CandidateQueueQueryService queueQueryService,
            ExamStaffSessionQueryService sessionQueryService,
            CallBoardSyncService callBoardSyncService) {
        this.queueQueryService = queueQueryService;
        this.sessionQueryService = sessionQueryService;
        this.callBoardSyncService = callBoardSyncService;
    }

    @Override
    public PublicCallSnapshotDTO loadSnapshot(int sessionId, String webRootPath, CallBoardState board) {
        PublicCallSnapshotDTO snapshot = new PublicCallSnapshotDTO();
        snapshot.setSessionId(sessionId);
        snapshot.setWaitingQueue(new ArrayList<>());
        snapshot.setUpdatedAtMs(System.currentTimeMillis());

        if (sessionId <= 0) {
            return snapshot;
        }

        List<ExamRegistrationDTO> queue = queueQueryService.listBySessionId(sessionId);
        queueQueryService.normalizePhotoPaths(webRootPath, queue);
        queue = callBoardSyncService.applyBoardOrder(queue, board);

        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        boolean deskBusy = board != null && board.isDeskBusy();
        String deskSbd = board != null ? board.getDeskSbd() : null;
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : System.currentTimeMillis();

        if ((nextSbd == null || nextSbd.isBlank()) && !shiftEnded) {
            nextSbd = CallBoardRules.resolveNextSbd(board, queue);
        }

        ExamRegistrationDTO callingCandidate = CallQueueRules.findBySbd(queue, callingSbd);
        ExamRegistrationDTO nextCandidate = CallQueueRules.findBySbd(queue, nextSbd);
        SessionDTO currentSession = sessionQueryService.findBySessionId(sessionId);

        snapshot.setCurrentSession(currentSession);
        snapshot.setCallingCandidate(callingCandidate);
        snapshot.setNextCandidate(nextCandidate);
        snapshot.setWaitingQueue(CallQueueRules.listWaitingTop(queue, 10));
        snapshot.setCallingActive(callingCandidate != null && !shiftEnded);
        snapshot.setShiftEnded(shiftEnded);
        snapshot.setUpdatedAtMs(updatedAtMs);
        snapshot.setDeskBusy(deskBusy);
        snapshot.setDeskSbd(deskSbd);
        return snapshot;
    }
}
