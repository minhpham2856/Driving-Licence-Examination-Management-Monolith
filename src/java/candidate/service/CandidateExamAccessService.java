package candidate.service;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import java.util.Map;

public interface CandidateExamAccessService {

    int loginExam(String examCodeOrId, String examPassword);

    boolean verifyExamPassword(int examId, String examPassword);

    CandidateExamContextDTO authenticate(int examId, String candidateNumber);

    // Specific reason authenticate() returned null, for display at the kiosk; null if SBD simply doesn't match.
    String resolveEntranceBlockReason(int examId, String candidateNumber);

    boolean start(CandidateExamContextDTO context);

    CandidateExamResultDTO submit(CandidateExamContextDTO context, Map<Integer, String> answers);
}
