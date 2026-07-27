package candidate.dao;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import java.util.Map;

public interface CandidateExamAccessDAO {

    int findActiveExamIdForLogin(String examCodeOrId, String examPassword);

    boolean verifyExamPassword(int examId, String examPassword);

    CandidateExamContextDTO getEligibleTheoryContext(int examId, String candidateNumber);

    boolean startTheoryAttempt(CandidateExamContextDTO context, int questionLimit);

    CandidateExamResultDTO submit(int theoryPaperId, CandidateExamContextDTO context,
            Map<Integer, String> answers);
}
