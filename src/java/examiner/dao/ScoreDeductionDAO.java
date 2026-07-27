package examiner.dao;

import shared.model.ScoreDeduction;

// DAO contract for ScoreDeduction persistence; examiner module SQL boundary.
public interface ScoreDeductionDAO {

    // Loads one score deduction rule row by primary key.
    ScoreDeduction get(int scoreDeductionId);
}
