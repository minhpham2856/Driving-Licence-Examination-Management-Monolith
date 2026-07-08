package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationActionResultDTO;
import dto.examstaff.AllocationCandidateActionRequest;

import java.util.List;

public interface AllocationActionService {

    AllocationActionResultDTO autoAllocateOnOverview(int sessionId, String stage);

    AllocationActionResultDTO executeAutoAllocate(int sessionId);

    AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request);

    ExamRegistrationDTO findCandidate(int regId, int sessionId, List<ExamRegistrationDTO> queue);
}
