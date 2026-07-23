package candidate.service;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import java.util.Map;

public interface CandidateExamAccessService {

    CandidateExamContextDTO authenticate(String candidateNumber, String otp);

    boolean start(CandidateExamContextDTO context);

    CandidateExamResultDTO submit(CandidateExamContextDTO context, Map<Integer, String> answers);
}
