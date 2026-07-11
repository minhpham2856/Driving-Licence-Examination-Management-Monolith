package service.impl;

import dao.CallBoardDAO;
import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;
import service.CallBoardSyncService;
import util.examstaff.CallBoardRules;
import util.examstaff.CallQueueRules;

import java.util.List;

public class CallBoardSyncServiceImpl implements CallBoardSyncService {

    @Override
    public CallBoardState getState(CallBoardDAO callBoardDAO, int examSessionId) {
        return callBoardDAO.getState(examSessionId);
    }

    @Override
    public void sync(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.syncBoard(
                callBoardDAO.getState(examSessionId), examSessionId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examSessionId, updated);
        callBoardDAO.setActiveSessionId(examSessionId);
    }

    @Override
    public void occupyDesk(CallBoardDAO callBoardDAO, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (examSessionId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        CallBoardState updated = CallBoardRules.occupyDesk(
                callBoardDAO.getState(examSessionId), examSessionId, deskSbd, queue, shiftEnded);
        callBoardDAO.saveState(examSessionId, updated);
        callBoardDAO.setActiveSessionId(examSessionId);
    }

    @Override
    public void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.releaseDeskAndCall(
                callBoardDAO.getState(examSessionId), examSessionId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examSessionId, updated);
        callBoardDAO.setActiveSessionId(examSessionId);
    }

    @Override
    public void pauseShift(CallBoardDAO callBoardDAO, int examSessionId, List<ExamRegistrationDTO> queue) {
        CallBoardState updated = CallBoardRules.pauseBoard(
                callBoardDAO.getState(examSessionId), examSessionId, queue);
        callBoardDAO.saveState(examSessionId, updated);
        callBoardDAO.setActiveSessionId(examSessionId);
    }

    @Override
    public List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board) {
        if (board == null || board.getQueueOrderSbds() == null || board.getQueueOrderSbds().isEmpty()) {
            return queue;
        }
        return CallQueueRules.applyQueueOrder(queue, board.getQueueOrderSbds());
    }
}
