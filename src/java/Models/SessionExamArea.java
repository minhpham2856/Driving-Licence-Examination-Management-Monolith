package Models;

public class SessionExamArea {
    private int sessionExamAreaId;
    private int sessionId;
    private int examAreaId;

    public SessionExamArea() {
    }

    public SessionExamArea(int sessionExamAreaId, int sessionId, int examAreaId) {
        this.sessionExamAreaId = sessionExamAreaId;
        this.sessionId = sessionId;
        this.examAreaId = examAreaId;
    }

    public int getSessionExamAreaId() {
        return sessionExamAreaId;
    }

    public void setSessionExamAreaId(int sessionExamAreaId) {
        this.sessionExamAreaId = sessionExamAreaId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }
}
