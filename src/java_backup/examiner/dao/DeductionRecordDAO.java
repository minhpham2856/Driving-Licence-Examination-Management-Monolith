package examiner.dao;

import shared.model.DeductionRecord;

import java.util.List;
import java.util.Map;

public interface DeductionRecordDAO {

    int getOccurrenceCount(int examScoreId, int scoreDeductionId);

    boolean add(DeductionRecord record);

    boolean updateOccurrence(int examScoreId, int scoreDeductionId, int occurrenceCount,
            java.sql.Timestamp recordedAt);

    boolean deleteByExamScoreAndRule(int examScoreId, int scoreDeductionId);

    // Top deduction reasons by occurrence (aggregated from DeductionRecord joined
    // to ScoreDeduction). Returns rows with keys "reason" and "count".
    List<Map<String, Object>> getTopReasons(int limit);
}

