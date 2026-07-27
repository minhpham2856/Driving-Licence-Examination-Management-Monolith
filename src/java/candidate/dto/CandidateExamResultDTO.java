package candidate.dto;

public class CandidateExamResultDTO {
    private int correct;
    private int wrong;
    private int unanswered;
    private boolean criticalFailed;
    private boolean passed;

    public int getCorrect() { return correct; }
    public void setCorrect(int value) { correct = value; }
    public int getWrong() { return wrong; }
    public void setWrong(int value) { wrong = value; }
    public int getUnanswered() { return unanswered; }
    public void setUnanswered(int value) { unanswered = value; }
    public boolean isCriticalFailed() { return criticalFailed; }
    public void setCriticalFailed(boolean value) { criticalFailed = value; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean value) { passed = value; }
}
