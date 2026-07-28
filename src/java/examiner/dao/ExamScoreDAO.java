package examiner.dao;

import shared.model.ExamScore;

// DAO contract for ExamScore persistence; examiner module SQL boundary.
public interface ExamScoreDAO {

    // Loads the score row for one exam result and section pair.
    ExamScore getByExamResultAndSection(int examResultId, int examSectionId);

    // Score for one enrollment + section type (e.g. Lý thuyết), or null if missing.
    Double getScoreByEnrollmentAndSectionType(int examEnrollmentId, String sectionType);

    // Inserts a new exam score row and returns generated id.
    int add(ExamScore score);

    // Updates only the Score column on one exam score row.
    boolean updateScore(int examScoreId, double score);

    // Recalculates score from DeductionRecord rows (critical rules force zero).
    boolean recalculateFromDeductions(int examScoreId);

    // Loads one exam score row by primary key.
    ExamScore get(int examScoreId);
}
