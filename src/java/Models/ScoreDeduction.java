package Models;

public class ScoreDeduction {
    private int id;
    private int practicalScoreId;
    private String deductionReason;
    private int deductionPoints;
    private String note;

    public ScoreDeduction() {
    }

    public ScoreDeduction(int id, int practicalScoreId, String deductionReason, int deductionPoints, String note) {
        this.id = id;
        this.practicalScoreId = practicalScoreId;
        this.deductionReason = deductionReason;
        this.deductionPoints = deductionPoints;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPracticalScoreId() {
        return practicalScoreId;
    }

    public void setPracticalScoreId(int practicalScoreId) {
        this.practicalScoreId = practicalScoreId;
    }

    public String getDeductionReason() {
        return deductionReason;
    }

    public void setDeductionReason(String deductionReason) {
        this.deductionReason = deductionReason;
    }

    public int getDeductionPoints() {
        return deductionPoints;
    }

    public void setDeductionPoints(int deductionPoints) {
        this.deductionPoints = deductionPoints;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
