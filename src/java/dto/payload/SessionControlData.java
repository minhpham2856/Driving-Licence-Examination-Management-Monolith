package dto.payload;

public class SessionControlData {

    private final String sessionName;
    private final int examinerCount;

    public SessionControlData(String sessionName, int examinerCount) {
        this.sessionName = sessionName;
        this.examinerCount = examinerCount;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getExaminerCount() {
        return examinerCount;
    }
}
