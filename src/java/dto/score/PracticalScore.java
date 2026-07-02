package dto.score;

import java.sql.Timestamp;

public class PracticalScore {
    private int id;
    private int examRegistrationId;
    private int examSectionId;
    private int baseScore = 100;
    private int totalDeductions;
    private int finalScore;
    private int evaluatedBy;
    private Timestamp evaluatedAt;

    public PracticalScore() {
    }

    public PracticalScore(int id, int examRegistrationId, int examSectionId, int baseScore, int totalDeductions, int finalScore, int evaluatedBy, Timestamp evaluatedAt) {
        this.id = id;
        this.examRegistrationId = examRegistrationId;
        this.examSectionId = examSectionId;
        this.baseScore = baseScore;
        this.totalDeductions = totalDeductions;
        this.finalScore = finalScore;
        this.evaluatedBy = evaluatedBy;
        this.evaluatedAt = evaluatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamRegistrationId() {
        return examRegistrationId;
    }

    public void setExamRegistrationId(int examRegistrationId) {
        this.examRegistrationId = examRegistrationId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(int baseScore) {
        this.baseScore = baseScore;
    }

    public int getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(int totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public int getEvaluatedBy() {
        return evaluatedBy;
    }

    public void setEvaluatedBy(int evaluatedBy) {
        this.evaluatedBy = evaluatedBy;
    }

    public Timestamp getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Timestamp evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
