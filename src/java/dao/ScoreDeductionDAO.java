package dao;

import model.ScoreDeduction;

public interface ScoreDeductionDAO {

    ScoreDeduction findById(int scoreDeductionId);

    int insert(ScoreDeduction deduction);

    boolean update(ScoreDeduction deduction);

    boolean delete(int scoreDeductionId);

    int countAll();

    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int scoreDeductionId, int delta);
}
