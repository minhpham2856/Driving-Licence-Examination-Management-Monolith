package dao;

import model.ExamScore;
import java.util.List;

public interface ExamScoreDAO {

    ExamScore findById(int examScoreId);

    int insert(ExamScore score);

    boolean update(ExamScore score);

    boolean delete(int examScoreId);

    int countAll();

    ExamScore getByCandidateId(int candidateId);

    boolean updateScores(int candidateId, int t, String tR, int p, String pR);
}
