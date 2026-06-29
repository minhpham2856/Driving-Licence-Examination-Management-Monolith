package dto;


import java.sql.Timestamp;

public class PracticalScoreDTO {
    private int id;
    private int examRegistrationId;
    private int examSectionId;
    private int baseScore = 100;
    private int totalDeductions;
    private int finalScore;
    private int evaluatedBy;
    private Timestamp evaluatedAt;

    public PracticalScoreDTO() {}
    public PracticalScoreDTO(int id, int examRegistrationId, int examSectionId, int baseScore, int totalDeductions, int finalScore, int evaluatedBy, Timestamp evaluatedAt) {
        this.id = id; this.examRegistrationId = examRegistrationId; this.examSectionId = examSectionId;
        this.baseScore = baseScore; this.totalDeductions = totalDeductions; this.finalScore = finalScore;
        this.evaluatedBy = evaluatedBy; this.evaluatedAt = evaluatedAt;
    }

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(int v) { this.examRegistrationId = v; }
    public int getExamSectionId() { return examSectionId; }
    public void setExamSectionId(int v) { this.examSectionId = v; }
    public int getBaseScore() { return baseScore; }
    public void setBaseScore(int v) { this.baseScore = v; }
    public int getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(int v) { this.totalDeductions = v; }
    public int getFinalScore() { return finalScore; }
    public void setFinalScore(int v) { this.finalScore = v; }
    public int getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(int v) { this.evaluatedBy = v; }
    public Timestamp getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Timestamp v) { this.evaluatedAt = v; }
}
