package model.exam;

import java.sql.Timestamp;

public class ExaminerSchedule {

    private int examinerScheduleId;
    private int sessionId;
    private int examinerId;
    private Integer examSectionId;
    private Integer examAreaId;
    private Integer assignedBy;
    private Timestamp assignedAt;

    public ExaminerSchedule() {
    }

    public ExaminerSchedule(int examinerScheduleId, int sessionId, int examinerId,
                            Integer examSectionId, Integer examAreaId,
                            Integer assignedBy, Timestamp assignedAt) {
        this.examinerScheduleId = examinerScheduleId;
        this.sessionId = sessionId;
        this.examinerId = examinerId;
        this.examSectionId = examSectionId;
        this.examAreaId = examAreaId;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public int getExaminerScheduleId() {
        return examinerScheduleId;
    }

    public void setExaminerScheduleId(int examinerScheduleId) {
        this.examinerScheduleId = examinerScheduleId;
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
