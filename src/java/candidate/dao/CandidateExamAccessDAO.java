package candidate.dao;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import java.util.List;
import java.util.Map;
import shared.model.Question;

public interface CandidateExamAccessDAO {

    CandidateExamContextDTO getEligibleTheoryContext(String candidateNumber);

    int startTheoryPaper(int examEnrollmentSectionId);

    List<Question> getRandomQuestions(int licenceId, int limit);

    CandidateExamResultDTO submit(int theoryPaperId, CandidateExamContextDTO context,
            Map<Integer, String> answers);
}
