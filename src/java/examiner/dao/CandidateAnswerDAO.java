package examiner.dao;

import shared.model.CandidateAnswer;
import java.util.List;

// DAO contract for CandidateAnswer persistence; examiner module SQL boundary.
public interface CandidateAnswerDAO {

    // Loads all answer rows for one theory paper.
    List<CandidateAnswer> getAllByTheoryPaperId(int theoryPaperId);
}
