package model;

public class ExamScore {

    private int examScoreId;
    private int examResultId;
    private int examSectionId;
    private double score;
    private ExamResult examResult;
    private ExamSection examSection;

    public ExamScore() {
    }

    public ExamScore(int examScoreId, int examResultId, int examSectionId, double score) {
        this.examScoreId = examScoreId;
        this.examResultId = examResultId;
        this.examSectionId = examSectionId;
        this.score = score;
    }

    public int getExamScoreId() {
        return examScoreId;
    }

    public void setExamScoreId(int examScoreId) {
        this.examScoreId = examScoreId;
    }

    public int getExamResultId() {
        return examResultId;
    }

    public void setExamResultId(int examResultId) {
        this.examResultId = examResultId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public ExamResult getExamResult() {
        return examResult;
    }

    public void setExamResult(ExamResult examResult) {
        this.examResult = examResult;
    }

    public ExamSection getExamSection() {
        return examSection;
    }

    public void setExamSection(ExamSection examSection) {
        this.examSection = examSection;
    }
}
