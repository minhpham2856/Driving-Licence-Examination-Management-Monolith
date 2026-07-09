package dto;

public class SessionStartDTO {

    private String sessionLabel;
    private int examinerCount;

    public SessionStartDTO() {
    }

    public String getSessionLabel() {
        return sessionLabel;
    }

    public void setsessionLabel(String sessionLabel) {
        this.sessionLabel = sessionLabel;
    }

    public int getExaminerCount() {
        return examinerCount;
    }

    public void setExaminerCount(int examinerCount) {
        this.examinerCount = examinerCount;
    }
}
