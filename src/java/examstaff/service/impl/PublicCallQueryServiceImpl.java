package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CallBoardSyncService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.service.PublicCallQueryService;
import examstaff.util.CallBoardRules;
import examstaff.util.CallQueueRules;

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
    public PublicCallSnapshotDTO loadSnapshot(int examId, String webRootPath, CallBoardState board) {
        PublicCallSnapshotDTO snapshot = new PublicCallSnapshotDTO();
        snapshot.setExamId(examId);
        snapshot.setWaitingQueue(new ArrayList<>());
        snapshot.setUpdatedAtMs(System.currentTimeMillis());

        if (examId <= 0) {
            return snapshot;
        }

        List<ExamRegistrationDTO> queue = queueQueryService.listByExamId(examId);
        queueQueryService.normalizePhotoPaths(webRootPath, queue);
        queue = callBoardSyncService.applyBoardOrder(queue, board);

        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        boolean examPaused = board != null && board.isExamPaused();
        boolean deskBusy = board != null && board.isDeskBusy();
        String deskSbd = board != null ? board.getDeskSbd() : null;
        long updatedAtMs = board != null ? board.getUpdatedAtMs() : System.currentTimeMillis();

        if ((nextSbd == null || nextSbd.isBlank()) && !shiftEnded && !examPaused) {
            nextSbd = CallBoardRules.resolveNextSbd(board, queue);
        }

        ExamRegistrationDTO callingCandidate = CallQueueRules.findBySbd(queue, callingSbd);
        ExamRegistrationDTO nextCandidate = CallQueueRules.findBySbd(queue, nextSbd);
        ExamSummaryDTO currentExam = sessionQueryService.findByExamId(examId);

        snapshot.setCurrentExam(currentExam);
        snapshot.setCallingCandidate(callingCandidate);
        snapshot.setNextCandidate(nextCandidate);
        snapshot.setWaitingQueue(CallQueueRules.listWaitingTop(queue, 10));
        snapshot.setCallingActive(callingCandidate != null && !shiftEnded && !examPaused);
        snapshot.setShiftEnded(shiftEnded);
        snapshot.setExamPaused(examPaused);
        snapshot.setUpdatedAtMs(updatedAtMs);
        snapshot.setDeskBusy(deskBusy);
        snapshot.setDeskSbd(deskSbd);
        return snapshot;
    }
}

