package examstaff.dto.score;


import java.sql.Timestamp;

public class TheoryScoreDTO {
    private int id;
    private int examPaperId;
    private int totalRawScore;
    private int finalScore;
    private Timestamp calculatedAt;

    public TheoryScoreDTO() {}
    public TheoryScoreDTO(int id, int examPaperId, int totalRawScore, int finalScore, Timestamp calculatedAt) {
        this.id = id; this.examPaperId = examPaperId; this.totalRawScore = totalRawScore;
        this.finalScore = finalScore; this.calculatedAt = calculatedAt;
    }

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getExamPaperId() { return examPaperId; }
    public void setExamPaperId(int v) { this.examPaperId = v; }
    public int getTotalRawScore() { return totalRawScore; }
    public void setTotalRawScore(int v) { this.totalRawScore = v; }
    public int getFinalScore() { return finalScore; }
    public void setFinalScore(int v) { this.finalScore = v; }
    public Timestamp getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Timestamp v) { this.calculatedAt = v; }
}
