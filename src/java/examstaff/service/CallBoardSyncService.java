package examstaff.service;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.model.view.CallBoardState;

import java.util.List;

public interface CallBoardSyncService {

    CallBoardState getState(CallBoardDAO callBoardDAO, int examId);

    void sync(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void pauseShift(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue);

    List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board);
}
