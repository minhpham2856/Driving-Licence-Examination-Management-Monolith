package model;

public class ScoreDeduction {
    private int scoreDeductionId;
    private String reason;
    private double points;
    private boolean isCritical;
    private Integer examSectionId;
    private int sortOrder;

    public int getScoreDeductionId() { return scoreDeductionId; }
    public void setScoreDeductionId(int scoreDeductionId) { this.scoreDeductionId = scoreDeductionId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public boolean isCritical() { return isCritical; }
    public void setCritical(boolean isCritical) { this.isCritical = isCritical; }
    public Integer getExamSectionId() { return examSectionId; }
    public void setExamSectionId(Integer examSectionId) { this.examSectionId = examSectionId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
