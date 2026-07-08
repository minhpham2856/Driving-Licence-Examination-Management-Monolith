package service;

import dto.examstaff.CandidateCallBoardStateDTO;
import dto.exam.ExamRegistrationDTO;
import repository.CallBoardRepository;

import java.util.List;

public interface CandidateCallBoardService {

    CandidateCallBoardStateDTO getState(CallBoardRepository repository, int examSessionId);

    void sync(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void occupyDesk(CallBoardRepository repository, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    void releaseDeskAndCall(CallBoardRepository repository, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);
}
