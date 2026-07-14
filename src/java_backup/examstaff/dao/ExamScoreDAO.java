package examstaff.dao;

import shared.model.ExamScore;

public interface ExamScoreDAO {

    ExamScore getByExamResultAndSection(int examResultId, int examSectionId);

    int add(ExamScore score);

    boolean updateScore(int examScoreId, double score);

    boolean recalculateFromDeductions(int examScoreId);
}

