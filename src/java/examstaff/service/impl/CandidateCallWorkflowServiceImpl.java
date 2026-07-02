package examstaff.service.impl;

import examstaff.dto.candidate.CandidateCallDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.service.CandidateAttendanceService;
import examstaff.service.CandidateCallRecordService;
import examstaff.service.CandidateCallWorkflowService;
import examstaff.service.CandidateQueueService;

import java.util.List;

public class CandidateCallWorkflowServiceImpl implements CandidateCallWorkflowService {

    private static final String CALLED_TO = "Bàn làm thủ tục số 2";

    private final CandidateQueueService queueService;
    private final CandidateCallRecordService callRecordService;
    private final CandidateAttendanceService attendanceService;

    public CandidateCallWorkflowServiceImpl() {
        this(new CandidateQueueServiceImpl(), new CandidateCallRecordServiceImpl(),
                new CandidateAttendanceServiceImpl());
    }

    public CandidateCallWorkflowServiceImpl(CandidateQueueService queueService,
            CandidateCallRecordService callRecordService,
            CandidateAttendanceService attendanceService) {
        this.queueService = queueService;
        this.callRecordService = callRecordService;
        this.attendanceService = attendanceService;
    }

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

    private void handleStartCall(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue,
            List<ExamRegistrationDTO> activeQueue, int calledByStaffId) {
        String startSbd = queueService.resolveNextCallingSbd(fullQueue, null);
        promoteCaller(result, activeQueue, startSbd, calledByStaffId);
    }

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

    private void handlePauseShift(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> fullQueue) {
        result.setClearCallingSbd(true);
        result.setShiftPaused(true);
        result.setShiftEnded(false);
        result.setSyncQueueOrder(true);
        result.setFullQueue(fullQueue);
        result.setActiveQueue(queueService.filterPendingForCall(fullQueue));
    }

    private void promoteCaller(CandidateCallActionResultDTO result, List<ExamRegistrationDTO> activeQueue,
            String nextSbd, int calledByStaffId) {
        if (nextSbd != null && !nextSbd.isBlank()) {
            result.setCallingSbd(nextSbd);
            recordCallingCandidate(activeQueue, nextSbd, calledByStaffId);
        } else {
            result.setClearCallingSbd(true);
        }
    }

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

    private void recordCall(ExamRegistrationDTO candidate, String callResult, int calledByStaffId) {
        CandidateCallDTO call = new CandidateCallDTO();
        call.setExamId(candidate.getExamId());
        call.setCandidateNo(candidate.getCandidateNo());
        call.setCalledTo(CALLED_TO);
        call.setCalledBy(calledByStaffId);
        call.setResult(callResult);
        callRecordService.recordCall(call);
    }
}
