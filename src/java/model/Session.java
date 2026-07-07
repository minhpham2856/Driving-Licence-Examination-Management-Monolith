package model;

import java.sql.Timestamp;

public class Session {

    private int sessionId;
    private boolean morningSession;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private int examId;
    private Exam exam;

    public Session() {
    }

    public Session(int sessionId, boolean morningSession, Timestamp startTime, Timestamp endTime, String status,
            int examId) {
        this.sessionId = sessionId;
        this.morningSession = morningSession;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.examId = examId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isMorningSession() {
        return morningSession;
    }

    public void setMorningSession(boolean morningSession) {
        this.morningSession = morningSession;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}
