package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.dto.CallBoardState;
import examstaff.service.CallBoardSyncService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.service.PublicCallQueryService;
import examstaff.util.CallBoardRules;
import examstaff.util.CallQueueRules;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây snapshot Public Call: đọc queue DB, áp thứ tự CallBoard, suy ra calling/next/waiting.
 */
public class PublicCallQueryServiceImpl implements PublicCallQueryService {

    private final CandidateQueueQueryService queueQueryService;
    private final ExamStaffExamQueryService examQueryService;
    private final CallBoardSyncService callBoardSyncService;

    /** Wiring mặc định khi không inject từ composition root. */
    public PublicCallQueryServiceImpl() {
        this(new CandidateQueueQueryServiceImpl(), new ExamStaffExamQueryServiceImpl(),
                new CallBoardSyncServiceImpl());
    }

    /**
     * @param queueQueryService   load hàng đợi theo examId
     * @param examQueryService    thông tin kỳ thi hiển thị
     * @param callBoardSyncService áp thứ tự board lên queue
     */
    public PublicCallQueryServiceImpl(CandidateQueueQueryService queueQueryService,
            ExamStaffExamQueryService examQueryService,
            CallBoardSyncService callBoardSyncService) {
        this.queueQueryService = queueQueryService;
        this.examQueryService = examQueryService;
        this.callBoardSyncService = callBoardSyncService;
    }

    /**
     * Ghép hàng đợi DB + trạng thái CallBoard thành snapshot hiển thị công khai.
     *
     * @param examId      mã kỳ thi đang active
     * @param webRootPath đường dẫn web root (chuẩn hóa URL ảnh)
     * @param board       trạng thái bảng gọi hiện tại (có thể null)
     * @return snapshot gồm thí sinh đang gọi, kế tiếp, hàng chờ và cờ pause/end
     */
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
            // Khi ban dang ban: khong tu resolve lai next (tranh goi chuan bi nguoi dang o ban).
            if (!deskBusy) {
                nextSbd = CallBoardRules.resolveNextSbd(board, queue);
            }
        }
        // Hardening: next khong duoc trung nguoi dang o ban / dang goi.
        if (nextSbd != null && !nextSbd.isBlank()) {
            if ((deskSbd != null && nextSbd.equals(deskSbd))
                    || (callingSbd != null && nextSbd.equals(callingSbd) && deskBusy)) {
                nextSbd = null;
            }
        }

        ExamRegistrationDTO callingCandidate = CallQueueRules.findBySbd(queue, callingSbd);
        ExamRegistrationDTO nextCandidate = CallQueueRules.findBySbd(queue, nextSbd);
        ExamSummaryDTO currentExam = examQueryService.findByExamId(examId);

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
