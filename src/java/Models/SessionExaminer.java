package Models;

import java.sql.Timestamp;

public class SessionExaminer {
    private int sessionExaminerId;
    private int sessionId;
    private int examinerId;
    private Integer examId;
    private Integer examSectionId;
    private Integer examAreaId;
    private Integer assignedBy;
    private Timestamp assignedAt;

    public SessionExaminer() {
    }

    public SessionExaminer(int sessionExaminerId, int sessionId, int examinerId, Integer examId, Integer examSectionId, Integer examAreaId, Integer assignedBy, Timestamp assignedAt) {
        this.sessionExaminerId = sessionExaminerId;
        this.sessionId = sessionId;
        this.examinerId = examinerId;
        this.examId = examId;
        this.examSectionId = examSectionId;
        this.examAreaId = examAreaId;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public int getSessionExaminerId() {
        return sessionExaminerId;
    }

    public void setSessionExaminerId(int sessionExaminerId) {
        this.sessionExaminerId = sessionExaminerId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getExaminerId() {
        return examinerId;
    }

    public void setExaminerId(int examinerId) {
        this.examinerId = examinerId;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(Integer examSectionId) {
        this.examSectionId = examSectionId;
    }

    public Integer getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(Integer examAreaId) {
        this.examAreaId = examAreaId;
    }

    public Integer getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Integer assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Timestamp getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }
}
