package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.CallBoardState;

import java.util.List;

public interface CandidateCallingService {

    ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue);

    String resolveSyncedCallingSbd(String sessionCallingSbd, CallBoardState callBoard,
            List<ExamRegistrationDTO> queue);

    String advanceCallingIfDone(String callingSbd, List<ExamRegistrationDTO> candidateQueue);
}

