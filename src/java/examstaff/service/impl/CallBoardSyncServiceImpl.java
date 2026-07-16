package examstaff.service.impl;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CallBoardSyncService;
import examstaff.util.CallBoardRules;
import examstaff.util.CallQueueRules;

import java.util.List;

public class CallBoardSyncServiceImpl implements CallBoardSyncService {

    @Override
    public CallBoardState getState(CallBoardDAO callBoardDAO, int examId) {
        return callBoardDAO.getState(examId);
    }

    @Override
    public void sync(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.syncBoard(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    @Override
    public void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (examId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        CallBoardState updated = CallBoardRules.occupyDesk(
                callBoardDAO.getState(examId), examId, deskSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    @Override
    public void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.releaseDeskAndCall(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    @Override
    public void pauseShift(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue) {
        CallBoardState updated = CallBoardRules.pauseBoard(
                callBoardDAO.getState(examId), examId, queue);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    @Override
    public List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board) {
        if (board == null || board.getQueueOrderSbds() == null || board.getQueueOrderSbds().isEmpty()) {
            return queue;
        }
        return CallQueueRules.applyQueueOrder(queue, board.getQueueOrderSbds());
    }
}
