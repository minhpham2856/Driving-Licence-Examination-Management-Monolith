package dao;

import model.*;

import model.DeductionRule;
import java.util.List;

public interface ScoreDeductionDAO {

    ScoreDeduction findById(int scoreDeductionId);

    int insert(ScoreDeduction deduction);

    boolean update(ScoreDeduction deduction);

    boolean delete(int scoreDeductionId);

    int countAll();

    boolean adjustScoreDeductionOccurrence(int candidateId, int ruleId, int adjustment, int amount);
}
