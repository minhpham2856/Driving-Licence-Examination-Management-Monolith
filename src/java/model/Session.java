package model;

import java.sql.Timestamp;

public class Session {
    private int id;
    private boolean morningSession;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private int examId;
    /** Nhãn hiển thị suy ra (Ca sáng/Ca chiều [- phần thi]) — không map cột DB. */
    private String sessionName;

    public Session() {
    }

    public Session(int id, boolean morningSession, Timestamp startTime, Timestamp endTime, String status, int examId) {
        this.id = id;
        this.morningSession = morningSession;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.examId = examId;
        this.sessionName = examstaff.util.SessionLabel.shiftLabel(morningSession);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSessionName() {
        if (sessionName != null && !sessionName.isBlank()) {
            return sessionName;
        }
        return examstaff.util.SessionLabel.shiftLabel(morningSession);
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
}
