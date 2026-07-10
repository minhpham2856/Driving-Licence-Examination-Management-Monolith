package Models;

public class ExamResultView {
    private boolean passed;
    private int score;         
    private int correctCount;
    private int incorrectCount;
    private int unansweredCount;
    private int totalQuestions;
    private boolean criticalFailed;
    private String startTime;
    private String endTime;

    public String getStatus() { return passed ? "PASSED" : "FAILED"; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean v) { this.passed = v; }
    public int getScore() { return score; }
    public void setScore(int v) { this.score = v; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int v) { this.correctCount = v; }
    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int v) { this.incorrectCount = v; }
    public int getUnansweredCount() { return unansweredCount; }
    public void setUnansweredCount(int v) { this.unansweredCount = v; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int v) { this.totalQuestions = v; }
    public boolean isCriticalFailed() { return criticalFailed; }
    public void setCriticalFailed(boolean v) { this.criticalFailed = v; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String v) { this.startTime = v; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String v) { this.endTime = v; }
}
