package model;
import java.sql.Timestamp;
public class ExaminerSchedule {
    private int examinerScheduleId;
    private int examId;
    private int examinerId;
    private Integer examSectionId;
    private Integer examAreaId;
    private Integer assignedBy;
    private Timestamp assignedAt;
    public ExaminerSchedule() {
    }
    public ExaminerSchedule(int examinerScheduleId, int examId, int examinerId,
                            Integer examSectionId, Integer examAreaId,
                            Integer assignedBy, Timestamp assignedAt) {
        this.examinerScheduleId = examinerScheduleId;
        this.examId = examId;
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
    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** @deprecated dùng {@link #getExamId()} */
    @Deprecated
    public int getSessionId() {
        return examId;
    }

    /** @deprecated dùng {@link #setExamId(int)} */
    @Deprecated
    public void setSessionId(int sessionId) {
        this.examId = sessionId;
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
