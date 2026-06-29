package dao.impl;

import model.*;

import dao.ScoreDeductionDAO;
import model.DeductionRule;
import java.util.ArrayList;
import java.util.List;

public class ScoreDeductionDAOImpl implements ScoreDeductionDAO {

    @Override
    public ScoreDeduction findById(int scoreDeductionId) { return null; }

    @Override
    public int insert(ScoreDeduction deduction) { return 0; }

    @Override
    public boolean update(ScoreDeduction deduction) { return false; }

    @Override
    public boolean delete(int scoreDeductionId) { return false; }

    @Override
    public int countAll() { return 0; }

    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int ruleId, int adjustment, int amount) { return false; }
}
