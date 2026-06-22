package Models;

public class ScoreDeduction {
    private int scoreDeductionId;
    private String reason;
    private double points;
    private boolean isCritical;
    private Integer examSectionId;
    private int sortOrder;

    public ScoreDeduction() {
    }

    public ScoreDeduction(int scoreDeductionId, String reason, double points, boolean isCritical, Integer examSectionId, int sortOrder) {
        this.scoreDeductionId = scoreDeductionId;
        this.reason = reason;
        this.points = points;
        this.isCritical = isCritical;
        this.examSectionId = examSectionId;
        this.sortOrder = sortOrder;
    }

    public int getScoreDeductionId() {
        return scoreDeductionId;
    }

    public void setScoreDeductionId(int scoreDeductionId) {
        this.scoreDeductionId = scoreDeductionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public boolean isIsCritical() {
        return isCritical;
    }

    public void setIsCritical(boolean isCritical) {
        this.isCritical = isCritical;
    }

    public Integer getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(Integer examSectionId) {
        this.examSectionId = examSectionId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
