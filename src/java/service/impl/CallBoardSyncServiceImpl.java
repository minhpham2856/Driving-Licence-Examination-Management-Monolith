package service.impl;

import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;
import repository.CallBoardRepository;
import service.CallBoardSyncService;
import util.examstaff.CallBoardRules;
import util.examstaff.CallQueueRules;

import java.util.List;

public class CallBoardSyncServiceImpl implements CallBoardSyncService {

    @Override
    public CallBoardState getState(CallBoardRepository repository, int examSessionId) {
        return repository.getState(examSessionId);
    }

    @Override
    public void sync(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.syncBoard(
                repository.getState(examSessionId), examSessionId, callingSbd, queue, shiftEnded);
        repository.saveState(examSessionId, updated);
        repository.setActiveSessionId(examSessionId);
    }

    @Override
    public void occupyDesk(CallBoardRepository repository, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (examSessionId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        CallBoardState updated = CallBoardRules.occupyDesk(
                repository.getState(examSessionId), examSessionId, deskSbd, queue, shiftEnded);
        repository.saveState(examSessionId, updated);
        repository.setActiveSessionId(examSessionId);
    }

    @Override
    public void releaseDeskAndCall(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.releaseDeskAndCall(
                repository.getState(examSessionId), examSessionId, callingSbd, queue, shiftEnded);
        repository.saveState(examSessionId, updated);
        repository.setActiveSessionId(examSessionId);
    }

    @Override
    public List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board) {
        if (board == null || board.getQueueOrderSbds() == null || board.getQueueOrderSbds().isEmpty()) {
            return queue;
        }
        return CallQueueRules.applyQueueOrder(queue, board.getQueueOrderSbds());
    }
}
