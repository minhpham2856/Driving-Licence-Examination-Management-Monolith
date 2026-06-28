package dao;

import model.exam.CandidateAnswer;
import java.util.List;

public interface CandidateAnswerDAO {
    List<CandidateAnswer> findByTheoryPaperId(int theoryPaperId);
    List<CandidateAnswer> findByTheoryPaperIds(List<Integer> theoryPaperIds);
}
