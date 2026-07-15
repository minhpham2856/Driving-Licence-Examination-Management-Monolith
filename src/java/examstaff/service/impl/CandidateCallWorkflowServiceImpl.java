package examstaff.service.impl;

import examstaff.dto.CandidateCallDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dao.CandidateCallDAO;
import examstaff.dao.impl.CandidateCallDAOImpl;
import examstaff.service.CandidateAttendanceService;
import examstaff.service.CandidateCallWorkflowService;
import examstaff.service.CandidateQueueService;

import java.util.List;

/**
 * Dispatch action gọi thí sinh (startCall, absent, pause, đóng ca…) sang handler tương ứng.
 */
public class CandidateCallWorkflowServiceImpl implements CandidateCallWorkflowService {

    private static final String CALLED_TO = "Bàn làm thủ tục số 2";

    private final CandidateQueueService queueService;
    private final CandidateCallDAO candidateCallDAO;
    private final CandidateAttendanceService attendanceService;

    /** Wiring mặc định. */
    public CandidateCallWorkflowServiceImpl() {
        this(new CandidateQueueServiceImpl(), new CandidateCallDAOImpl(),
                new CandidateAttendanceServiceImpl());
    }

    /**
     * @param queueService      thao tác hàng đợi
     * @param candidateCallDAO  ghi audit CALL
     * @param attendanceService đánh vắng / đình chỉ / restore
     */
    public CandidateCallWorkflowServiceImpl(CandidateQueueService queueService,
            CandidateCallDAO candidateCallDAO,
            CandidateAttendanceService attendanceService) {
        this.queueService = queueService;
        this.candidateCallDAO = candidateCallDAO;
        this.attendanceService = attendanceService;
    }

    /**
     * Chạy một action gọi thí sinh và trả kết quả side-effect cho orchestrator trang.
     *
     * @param action           mã action ({@code startCall}, {@code absent}, {@code pauseShift}…)
     * @param sbd              SBD liên quan (nếu có)
     * @param fullQueue        hàng đợi đầy đủ (có thể bị sửa thứ tự in-place)
     * @param permanentAbsents danh sách đình chỉ trên session
     * @param boardExamId      kỳ thi trên bảng gọi
     * @param shiftEnded       ca đã đóng hay chưa
     * @param calledByStaffId  userId staff đang thao tác
     * @return kết quả: cập nhật callingSbd, alert, cờ reload/sync/promote…
     */
    @Override
    public CandidateCallActionResultDTO executeAction(String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId, boolean shiftEnded, int calledByStaffId) {
        CandidateCallActionResultDTO result = new CandidateCallActionResultDTO();
        result.setFullQueue(fullQueue);
        result.setShiftEnded(shiftEnded);

        if (fullQueue == null) {
            result.setActiveQueue(List.of());
            return result;
        }

        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        result.setActiveQueue(activeQueue);

        if (action == null) {
            return result;
        }

        switch (action) {
            case "startCall" -> handleStartCall(result, fullQueue, activeQueue, calledByStaffId);
            case "moveToBottom", "absent", "autoAbsent" -> handleAbsentAction(
                    result, action, sbd, fullQueue, boardExamId, calledByStaffId);
            case "permanentAbsent" -> handlePermanentAbsent(
                    result, sbd, fullQueue, permanentAbsents, calledByStaffId);
            case "undoAbsent" -> handleUndoAbsent(
                    result, sbd, fullQueue, permanentAbsents, boardExamId);
            case "endShift", "closeExam" -> handleEndShift(result, fullQueue, permanentAbsents);
            case "pauseShift" -> handlePauseShift(result, fullQueue);
            case "startShift" -> result.setRedirectToCallPage(true);
            default -> {
            }
        }

        if (result.getActiveQueue() == null) {
            result.setActiveQueue(queueService.filterPendingForCall(result.getFullQueue()));
        }
        return result;
    }

    /** Bắt đầu ca gọi: lấy SBD pending đầu hàng và promote. */
    private void handleStartCall(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue,
            List<ExamRegistrationDTO> activeQueue, int calledByStaffId) {
        String startSbd = queueService.resolveNextCallingSbd(fullQueue, null);
        promoteCaller(result, activeQueue, startSbd, calledByStaffId);
    }

    /**
     * Vắng / đẩy xuống cuối hàng ({@code absent}, {@code autoAbsent}, {@code moveToBottom}).
     * Ghi audit Absent rồi promote người kế.
     */
    private void handleAbsentAction(CandidateCallActionResultDTO result, String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, int boardExamId, int calledByStaffId) {
        if (sbd == null || sbd.isBlank()) {
            return;
        }
        ExamRegistrationDTO moved = queueService.findBySbd(fullQueue, sbd);
        if (moved == null || !queueService.moveCallableCandidateToBottom(fullQueue, sbd)) {
            return;
        }

        recordCall(moved, "Absent", calledByStaffId);
        result.setSyncQueueOrder(true);
        result.setPromoteAfterSbd(sbd);
        result.setAlertType("autoAbsent".equals(action)
                ? CandidateCallActionResultDTO.AlertType.AUTO_ABSENT
                : CandidateCallActionResultDTO.AlertType.ABSENT);
        result.setAlertSbd(sbd);
    }

    /** Đình chỉ thí sinh (permanent absent) và yêu cầu reload queue + promote. */
    private void handlePermanentAbsent(CandidateCallActionResultDTO result, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int calledByStaffId) {
        ExamRegistrationDTO removed = queueService.findBySbd(fullQueue, sbd);
        if (removed == null) {
            result.setReloadQueue(true);
            return;
        }

        attendanceService.markPermanentAbsent(removed.getId());
        removed.setSuspended(true);
        removed.setAbsent(true);

        result.setReloadQueue(true);
        result.setPromoteAfterSbd(sbd);
        result.setAlertType(CandidateCallActionResultDTO.AlertType.PERMANENT_ABSENT);
        result.setAlertSbd(sbd);
    }

    /** Hoàn tác vắng/đình chỉ, đưa SBD về đầu hàng đợi gọi. */
    private void handleUndoAbsent(CandidateCallActionResultDTO result, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId) {
        ExamRegistrationDTO restored = queueService.findBySbd(fullQueue, sbd);
        if (restored == null && permanentAbsents != null) {
            for (int i = 0; i < permanentAbsents.size(); i++) {
                if (sbd.equals(permanentAbsents.get(i).getSbd())) {
                    restored = permanentAbsents.remove(i);
                    break;
                }
            }
        }
        if (restored == null || (!restored.isSuspended() && !restored.isAbsent())) {
            return;
        }

        attendanceService.restoreAbsentCandidate(restored);
        result.setReloadQueue(true);
        result.setCallingSbd(sbd);
        result.setMoveRestoredToFront(true);
        result.setAlertType(CandidateCallActionResultDTO.AlertType.UNDO);
        result.setAlertSbd(sbd);
    }

    /** Đóng ca gọi: đánh vắng các pending còn lại, clear số đang gọi. */
    private void handleEndShift(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue,
            List<ExamRegistrationDTO> permanentAbsents) {
        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        List<ExamRegistrationDTO> marked = attendanceService.markIncompleteAsAbsentAtEndShift(activeQueue);
        if (permanentAbsents != null) {
            permanentAbsents.addAll(marked);
        }
        activeQueue.removeAll(marked);

        result.setClearCallingSbd(true);
        result.setShiftEnded(true);
        result.setShiftPaused(false);
        result.setReloadQueue(true);
        result.setActiveQueue(activeQueue);
    }

    /** Tạm dừng ca gọi: clear calling, giữ queue, đánh dấu paused. */
    private void handlePauseShift(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue) {
        result.setClearCallingSbd(true);
        result.setShiftPaused(true);
        result.setShiftEnded(false);
        result.setSyncQueueOrder(true);
        result.setFullQueue(fullQueue);
        result.setActiveQueue(queueService.filterPendingForCall(fullQueue));
    }

    /** Gắn callingSbd mới vào result và ghi audit Calling. */
    private void promoteCaller(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> activeQueue,
            String nextSbd, int calledByStaffId) {
        if (nextSbd != null && !nextSbd.isBlank()) {
            result.setCallingSbd(nextSbd);
            recordCallingCandidate(activeQueue, nextSbd, calledByStaffId);
        } else {
            result.setClearCallingSbd(true);
        }
    }

    /**
     * Ghi nhận lượt gọi thí sinh (audit CALL) khi promote SBD lên số đang gọi.
     *
     * @param activeQueue     hàng đợi còn pending
     * @param nextSbd         SBD được gọi
     * @param calledByStaffId userId staff
     */
    @Override
    public void recordCallingCandidate(List<ExamRegistrationDTO> activeQueue, String nextSbd, int calledByStaffId) {
        if (nextSbd == null || nextSbd.isBlank()) {
            return;
        }
        ExamRegistrationDTO next = queueService.findBySbd(activeQueue, nextSbd);
        if (next != null) {
            recordCall(next, "Calling", calledByStaffId);
        }
    }

    /** Map sang {@link CandidateCallDTO} rồi ghi qua {@link CandidateCallDAO}. */
    private void recordCall(ExamRegistrationDTO candidate, String callResult, int calledByStaffId) {
        CandidateCallDTO call = new CandidateCallDTO();
        call.setExamId(candidate.getExamId());
        call.setCandidateNo(candidate.getCandidateNo());
        call.setCalledTo(CALLED_TO);
        call.setCalledBy(calledByStaffId);
        call.setResult(callResult);
        candidateCallDAO.insert(call);
    }
}
