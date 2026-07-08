package service;

import dao.CallBoardDAO;
import dto.examstaff.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidateCallBoardService {

    CandidateCallBoardStateDTO getState(CallBoardDAO callBoardDAO, int examSessionId);

    void sync(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void occupyDesk(CallBoardDAO callBoardDAO, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);
}
