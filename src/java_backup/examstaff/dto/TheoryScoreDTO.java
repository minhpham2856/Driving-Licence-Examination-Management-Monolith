package examstaff.dto;
import java.sql.Timestamp;
public class TheoryScoreDTO {
    private int id;
    private int examPaperId;
    private int totalRawScore;
    private int finalScore;
    private Timestamp calculatedAt;
    public TheoryScoreDTO() {}
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
