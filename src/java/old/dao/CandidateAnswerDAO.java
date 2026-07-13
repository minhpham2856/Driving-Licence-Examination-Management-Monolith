package dao;

import model.CandidateAnswer;
import java.util.List;

public interface CandidateAnswerDAO {

    List<CandidateAnswer> findByTheoryPaperId(int theoryPaperId);
}
