package dao;

import model.DeductionRecord;

public interface DeductionRecordDAO {

    int getOccurrenceCount(int examScoreId, int scoreDeductionId);

    boolean add(DeductionRecord record);

    boolean updateOccurrence(int examScoreId, int scoreDeductionId, int occurrenceCount,
            java.sql.Timestamp recordedAt);

    boolean deleteByExamScoreAndRule(int examScoreId, int scoreDeductionId);
}
