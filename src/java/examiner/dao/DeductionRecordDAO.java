package examiner.dao;

import shared.model.DeductionRecord;

import java.util.List;
import java.util.Map;

// DAO contract for DeductionRecord persistence; examiner module SQL boundary.
public interface DeductionRecordDAO {

    // Returns occurrence count for one exam score and deduction rule pair.
    int getOccurrenceCount(int examScoreId, int scoreDeductionId);

    // Inserts a new deduction occurrence row.
    boolean add(DeductionRecord record);

    // Updates occurrence count and recorded timestamp for one score/rule pair.
    boolean updateOccurrence(int examScoreId, int scoreDeductionId, int occurrenceCount,
            java.sql.Timestamp recordedAt);

    // Deletes the deduction row for one exam score and rule.
    boolean deleteByExamScoreAndRule(int examScoreId, int scoreDeductionId);
}
