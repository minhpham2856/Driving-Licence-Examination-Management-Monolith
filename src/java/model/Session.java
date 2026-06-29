package model;

import java.sql.Timestamp;

public class Session {

    private int id;
    private String sessionName;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private int examId;

    public Session() {
    }

    public Session(int id, String sessionName, Timestamp startTime, Timestamp endTime, String status, int examId) {
        this.id = id;
        this.sessionName = sessionName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.examId = examId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
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
}
