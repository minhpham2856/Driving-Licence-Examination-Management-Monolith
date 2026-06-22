package Models;

public class ExaminerSchedule {

    private int examinerScheduleId;
    private int sessionId;
    private int examinerId;
    private Integer examSectionId;
    private Integer examAreaId;
    private Integer assignedBy;
    private java.sql.Timestamp assignedAt;

    public ExaminerSchedule() {
    }

    public int getExaminerScheduleId() {
        return examinerScheduleId;
    }

    public void setExaminerScheduleId(int v) {
        this.examinerScheduleId = v;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int v) {
        this.sessionId = v;
    }

    public int getExaminerId() {
        return examinerId;
    }

    public void setExaminerId(int v) {
        this.examinerId = v;
    }

    public Integer getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(Integer v) {
        this.examSectionId = v;
    }

    public Integer getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(Integer v) {
        this.examAreaId = v;
    }

    public Integer getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Integer v) {
        this.assignedBy = v;
    }

    public java.sql.Timestamp getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(java.sql.Timestamp v) {
        this.assignedAt = v;
    }
}
