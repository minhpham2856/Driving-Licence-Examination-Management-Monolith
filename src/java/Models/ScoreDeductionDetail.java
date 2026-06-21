package Models;

import java.sql.Timestamp;

public class ScoreDeductionDetail {

    private int scoreDeductionDetailId;
    private int examScoreId;
    private int scoreDeductionId;
    private int occurrenceCount;
    private Timestamp recordedAt;

    public ScoreDeductionDetail() {
    }

    public ScoreDeductionDetail(int scoreDeductionDetailId, int examScoreId, int scoreDeductionId, int occurrenceCount, Timestamp recordedAt) {
        this.scoreDeductionDetailId = scoreDeductionDetailId;
        this.examScoreId = examScoreId;
        this.scoreDeductionId = scoreDeductionId;
        this.occurrenceCount = occurrenceCount;
        this.recordedAt = recordedAt;
    }

    public int getScoreDeductionDetailId() {
        return scoreDeductionDetailId;
    }

    public void setScoreDeductionDetailId(int scoreDeductionDetailId) {
        this.scoreDeductionDetailId = scoreDeductionDetailId;
    }

    public int getExamScoreId() {
        return examScoreId;
    }

    public void setExamScoreId(int examScoreId) {
        this.examScoreId = examScoreId;
    }

    public int getScoreDeductionId() {
        return scoreDeductionId;
    }

    public void setScoreDeductionId(int scoreDeductionId) {
        this.scoreDeductionId = scoreDeductionId;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public Timestamp getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Timestamp recordedAt) {
        this.recordedAt = recordedAt;
    }
}
