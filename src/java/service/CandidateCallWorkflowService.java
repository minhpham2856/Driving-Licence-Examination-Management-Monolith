package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateCallActionResultDTO;

import java.util.List;

public interface CandidateCallWorkflowService {

    CandidateCallActionResultDTO executeAction(String action, String sbd,
            List<ExamRegistrationDTO> fullQueue, List<ExamRegistrationDTO> permanentAbsents,
            int boardExamId, boolean shiftEnded, int calledByStaffId);

    void recordCallingCandidate(List<ExamRegistrationDTO> activeQueue, String nextSbd, int calledByStaffId);
}
