package candidate.service;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import java.util.Map;

public interface CandidateExamAccessService {

    int loginExam(String examCodeOrId, String examPassword);

    boolean verifyExamPassword(int examId, String examPassword);

    CandidateExamContextDTO authenticate(int examId, String candidateNumber);

    boolean start(CandidateExamContextDTO context);

    CandidateExamResultDTO submit(CandidateExamContextDTO context, Map<Integer, String> answers);
}
