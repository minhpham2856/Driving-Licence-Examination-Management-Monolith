package model.exam;
import java.sql.Timestamp;

public class DeductionRecord {
    private int deductionRecordId;
    private int examScoreId;
    private int scoreDeductionId;
    private int occurrenceCount;
    private Timestamp recordedAt;

    public int getDeductionRecordId() { return deductionRecordId; }
    public void setDeductionRecordId(int deductionRecordId) { this.deductionRecordId = deductionRecordId; }
    public int getExamScoreId() { return examScoreId; }
    public void setExamScoreId(int examScoreId) { this.examScoreId = examScoreId; }
    public int getScoreDeductionId() { return scoreDeductionId; }
    public void setScoreDeductionId(int scoreDeductionId) { this.scoreDeductionId = scoreDeductionId; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }
}
