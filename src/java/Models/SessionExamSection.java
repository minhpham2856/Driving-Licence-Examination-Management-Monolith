package Models;

public class SessionExamSection {
    private int sessionExamSectionId;
    private int sessionId;
    private int examSectionId;

    public SessionExamSection() {
    }

    public SessionExamSection(int sessionExamSectionId, int sessionId, int examSectionId) {
        this.sessionExamSectionId = sessionExamSectionId;
        this.sessionId = sessionId;
        this.examSectionId = examSectionId;
    }

    public int getSessionExamSectionId() {
        return sessionExamSectionId;
    }

    public void setSessionExamSectionId(int sessionExamSectionId) {
        this.sessionExamSectionId = sessionExamSectionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }
}
