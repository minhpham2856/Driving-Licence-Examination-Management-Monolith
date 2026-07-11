package service;

import dao.CallBoardDAO;
import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;

import java.util.List;

public interface CallBoardSyncService {

    CallBoardState getState(CallBoardDAO callBoardDAO, int examSessionId);

    void sync(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void occupyDesk(CallBoardDAO callBoardDAO, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void pauseShift(CallBoardDAO callBoardDAO, int examSessionId, List<ExamRegistrationDTO> queue);

    List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board);
}
