package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;

import java.util.List;

public interface AllocationActionService {

    AllocationActionResultDTO autoAllocateOnOverview(int examId, String stage);

    AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request);

    ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue);
}
