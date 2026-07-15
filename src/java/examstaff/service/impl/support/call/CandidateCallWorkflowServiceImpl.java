package examstaff.service.impl.support.call;

import examstaff.dto.CandidateCallDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import java.util.List;

/**
 * Dispatch action gọi thí sinh (startCall, absent, pause, đóng ca…) sang handler tương ứng.
 */
public class CandidateCallWorkflowServiceImpl {

    private static final String CALLED_TO = "Bàn làm thủ tục số 2";

    private final CandidateQueueServiceImpl queueService;
    private final AuditLogDAO auditLogDAO;
    private final CandidateAttendanceServiceImpl attendanceService;

    /** Wiring mặc định khi không inject từ composition root. */
    public CandidateCallWorkflowServiceImpl() {
        this(new CandidateQueueServiceImpl(), new AuditLogDAOImpl(),
                new CandidateAttendanceServiceImpl());
    }

    /**
     * Inject dependencies cho unit test / composition root.
     *
     * @param queueService      thao tác hàng đợi
     * @param auditLogDAO       ghi audit CALL
     * @param attendanceService đánh vắng / đình chỉ / restore
     */
    public CandidateCallWorkflowServiceImpl(CandidateQueueServiceImpl queueService,
            AuditLogDAO auditLogDAO,
            CandidateAttendanceServiceImpl attendanceService) {
        this.queueService = queueService;
        this.auditLogDAO = auditLogDAO;
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
    public CandidateCallActionResultDTO executeAction(String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId, boolean shiftEnded, int calledByStaffId) {
        // Load / khởi tạo result
        CandidateCallActionResultDTO result = new CandidateCallActionResultDTO();
        result.setFullQueue(fullQueue);
        result.setShiftEnded(shiftEnded);

        // Validate hàng đợi
        if (fullQueue == null) {
            result.setActiveQueue(List.of());
            return result;
        }

        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        result.setActiveQueue(activeQueue);

        if (action == null) {
            return result;
        }

        // Mutate: dispatch theo mã action
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

        // Result: đảm bảo activeQueue luôn có sau mutate
        if (result.getActiveQueue() == null) {
            result.setActiveQueue(queueService.filterPendingForCall(result.getFullQueue()));
        }
        return result;
    }

    /**
     * Bắt đầu ca gọi: lấy SBD pending đầu hàng và promote.
     *
     * @param result          kết quả action đang dựng
     * @param fullQueue       hàng đợi đầy đủ
     * @param activeQueue    hàng pending
     * @param calledByStaffId userId staff
     */
    private void handleStartCall(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue,
            List<ExamRegistrationDTO> activeQueue, int calledByStaffId) {
        String startSbd = queueService.resolveNextCallingSbd(fullQueue, null);
        promoteCaller(result, activeQueue, startSbd, calledByStaffId);
    }

    /**
     * Vắng / đẩy xuống cuối hàng ({@code absent}, {@code autoAbsent}, {@code moveToBottom}).
     * Ghi audit Absent rồi yêu cầu promote người kế.
     *
     * @param result          kết quả action
     * @param action          mã action gốc (phân biệt autoAbsent)
     * @param sbd             SBD bị vắng
     * @param fullQueue       hàng đợi (sửa thứ tự in-place)
     * @param boardExamId     kỳ thi board (giữ chữ ký API)
     * @param calledByStaffId userId staff
     */
    private void handleAbsentAction(CandidateCallActionResultDTO result, String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, int boardExamId, int calledByStaffId) {
        // Validate
        if (sbd == null || sbd.isBlank()) {
            return;
        }
        ExamRegistrationDTO moved = queueService.findBySbd(fullQueue, sbd);
        if (moved == null || !queueService.moveCallableCandidateToBottom(fullQueue, sbd)) {
            return;
        }

        // Mutate + audit + cờ promote
        recordCall(moved, "Absent", calledByStaffId);
        result.setSyncQueueOrder(true);
        result.setPromoteAfterSbd(sbd);
        result.setAlertType("autoAbsent".equals(action)
                ? CandidateCallActionResultDTO.AlertType.AUTO_ABSENT
                : CandidateCallActionResultDTO.AlertType.ABSENT);
        result.setAlertSbd(sbd);
    }

    /**
     * Đình chỉ thí sinh (permanent absent) và yêu cầu reload queue + promote.
     *
     * @param result           kết quả action
     * @param sbd              SBD bị đình chỉ
     * @param fullQueue        hàng đợi
     * @param permanentAbsents list session (giữ chữ ký API)
     * @param calledByStaffId  userId staff
     */
    private void handlePermanentAbsent(CandidateCallActionResultDTO result, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int calledByStaffId) {
        // Load
        ExamRegistrationDTO removed = queueService.findBySbd(fullQueue, sbd);
        if (removed == null) {
            result.setReloadQueue(true);
            return;
        }

        // Mutate DB + DTO in-memory
        attendanceService.markPermanentAbsent(removed.getId());
        removed.setSuspended(true);
        removed.setAbsent(true);

        // Result flags
        result.setReloadQueue(true);
        result.setPromoteAfterSbd(sbd);
        result.setAlertType(CandidateCallActionResultDTO.AlertType.PERMANENT_ABSENT);
        result.setAlertSbd(sbd);
    }

    /**
     * Hoàn tác vắng/đình chỉ, đưa SBD về đầu hàng đợi gọi.
     *
     * @param result           kết quả action
     * @param sbd              SBD cần restore
     * @param fullQueue        hàng đợi
     * @param permanentAbsents list đình chỉ trên session
     * @param boardExamId      kỳ thi board (giữ chữ ký API)
     */
    private void handleUndoAbsent(CandidateCallActionResultDTO result, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId) {
        // Load: tìm trong queue hoặc list permanentAbsents
        ExamRegistrationDTO restored = queueService.findBySbd(fullQueue, sbd);
        if (restored == null && permanentAbsents != null) {
            for (int i = 0; i < permanentAbsents.size(); i++) {
                if (sbd.equals(permanentAbsents.get(i).getSbd())) {
                    restored = permanentAbsents.remove(i);
                    break;
                }
            }
        }
        // Validate trạng thái vắng/đình chỉ
        if (restored == null || (!restored.isSuspended() && !restored.isAbsent())) {
            return;
        }

        // Mutate + Result
        attendanceService.restoreAbsentCandidate(restored);
        result.setReloadQueue(true);
        result.setCallingSbd(sbd);
        result.setMoveRestoredToFront(true);
        result.setAlertType(CandidateCallActionResultDTO.AlertType.UNDO);
        result.setAlertSbd(sbd);
    }

    /**
     * Đóng ca gọi: đánh vắng các pending còn lại, clear số đang gọi.
     *
     * @param result           kết quả action
     * @param fullQueue        hàng đợi đầy đủ
     * @param permanentAbsents list nhận các thí sinh vừa đánh vắng
     */
    private void handleEndShift(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue,
            List<ExamRegistrationDTO> permanentAbsents) {
        // Load active + mutate đánh vắng unfinished
        List<ExamRegistrationDTO> activeQueue = queueService.filterPendingForCall(fullQueue);
        List<ExamRegistrationDTO> marked = attendanceService.markIncompleteAsAbsentAtEndShift(activeQueue);
        if (permanentAbsents != null) {
            permanentAbsents.addAll(marked);
        }
        activeQueue.removeAll(marked);

        // Result flags
        result.setClearCallingSbd(true);
        result.setShiftEnded(true);
        result.setShiftPaused(false);
        result.setReloadQueue(true);
        result.setActiveQueue(activeQueue);
    }

    /**
     * Tạm dừng ca gọi: clear calling, giữ queue, đánh dấu paused.
     *
     * @param result    kết quả action
     * @param fullQueue hàng đợi đầy đủ
     */
    private void handlePauseShift(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue) {
        result.setClearCallingSbd(true);
        result.setShiftPaused(true);
        result.setShiftEnded(false);
        result.setSyncQueueOrder(true);
        result.setFullQueue(fullQueue);
        result.setActiveQueue(queueService.filterPendingForCall(fullQueue));
    }

    /**
     * Gắn callingSbd mới vào result và ghi audit Calling.
     *
     * @param result          kết quả action
     * @param activeQueue     hàng pending
     * @param nextSbd         SBD promote (blank → clear)
     * @param calledByStaffId userId staff
     */
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
    public void recordCallingCandidate(List<ExamRegistrationDTO> activeQueue, String nextSbd, int calledByStaffId) {
        // Validate
        if (nextSbd == null || nextSbd.isBlank()) {
            return;
        }
        ExamRegistrationDTO next = queueService.findBySbd(activeQueue, nextSbd);
        if (next != null) {
            recordCall(next, "Calling", calledByStaffId);
        }
    }

    /**
     * Map sang {@link CandidateCallDTO} rồi ghi qua {@link AuditLogDAO#insertCall}.
     *
     * @param candidate       hồ sơ thí sinh
     * @param callResult      kết quả gọi ({@code Calling}, {@code Absent}, …)
     * @param calledByStaffId userId staff
     */
    private void recordCall(ExamRegistrationDTO candidate, String callResult, int calledByStaffId) {
        CandidateCallDTO call = new CandidateCallDTO();
        call.setExamId(candidate.getExamId());
        call.setCandidateNo(candidate.getCandidateNo());
        call.setCalledTo(CALLED_TO);
        call.setCalledBy(calledByStaffId);
        call.setResult(callResult);
        auditLogDAO.insertCall(call);
    }
}
