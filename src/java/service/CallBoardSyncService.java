package service;

import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;
import repository.CallBoardRepository;

import java.util.List;

public interface CallBoardSyncService {

    CallBoardState getState(CallBoardRepository repository, int examSessionId);

    void sync(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void occupyDesk(CallBoardRepository repository, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void releaseDeskAndCall(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board);
}
