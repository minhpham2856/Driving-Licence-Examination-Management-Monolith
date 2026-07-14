package examiner.dao;

import shared.model.CandidateAnswer;
import java.util.List;

public interface CandidateAnswerDAO {

    List<CandidateAnswer> findByTheoryPaperId(int theoryPaperId);
}

