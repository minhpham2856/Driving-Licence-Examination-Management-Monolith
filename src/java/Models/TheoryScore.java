package Models;

import java.sql.Timestamp;

public class TheoryScore {
    private int id;
    private int examPaperId;
    private int totalRawScore;
    private int finalScore;
    private Timestamp calculatedAt;

    public TheoryScore() {
    }

    public TheoryScore(int id, int examPaperId, int totalRawScore, int finalScore, Timestamp calculatedAt) {
        this.id = id;
        this.examPaperId = examPaperId;
        this.totalRawScore = totalRawScore;
        this.finalScore = finalScore;
        this.calculatedAt = calculatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(int examPaperId) {
        this.examPaperId = examPaperId;
    }

    public int getTotalRawScore() {
        return totalRawScore;
    }

    public void setTotalRawScore(int totalRawScore) {
        this.totalRawScore = totalRawScore;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public Timestamp getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Timestamp calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
