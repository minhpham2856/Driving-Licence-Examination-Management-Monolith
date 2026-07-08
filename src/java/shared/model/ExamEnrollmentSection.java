package shared.model;

import java.sql.Timestamp;

public class ExamEnrollmentSection {

    private int examEnrollmentSectionId;
    private int examEnrollmentId;
    private Integer examSectionId;
    private Integer examAreaId;
    private Integer examDeviceId;
    private String status; // Pending | InProgress | Completed
    private Timestamp allocatedAt;
    private Integer allocatedBy;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private Timestamp resultPrintedAt;
    private ExamEnrollment examEnrollment;
    private ExamSection examSection;
    private ExamArea examArea;
    private ExamDevice examDevice;
    private User allocatedByUser;

    public ExamEnrollmentSection() {
    }

    public int getExamEnrollmentSectionId() {
        return examEnrollmentSectionId;
    }

    public void setExamEnrollmentSectionId(int v) {
        this.examEnrollmentSectionId = v;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int v) {
        this.examEnrollmentId = v;
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

    public Integer getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(Integer v) {
        this.examDeviceId = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public Timestamp getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(Timestamp v) {
        this.allocatedAt = v;
    }

    public Integer getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(Integer v) {
        this.allocatedBy = v;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp v) {
        this.startedAt = v;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp v) {
        this.completedAt = v;
    }

    public Timestamp getResultPrintedAt() {
        return resultPrintedAt;
    }

    public void setResultPrintedAt(Timestamp v) {
        this.resultPrintedAt = v;
    }

    public ExamEnrollment getExamEnrollment() {
        return examEnrollment;
    }

    public void setExamEnrollment(ExamEnrollment v) {
        this.examEnrollment = v;
    }

    public ExamSection getExamSection() {
        return examSection;
    }

    public void setExamSection(ExamSection v) {
        this.examSection = v;
    }

    public ExamArea getExamArea() {
        return examArea;
    }

    public void setExamArea(ExamArea v) {
        this.examArea = v;
    }

    public ExamDevice getExamDevice() {
        return examDevice;
    }

    public void setExamDevice(ExamDevice v) {
        this.examDevice = v;
    }

    public User getAllocatedByUser() {
        return allocatedByUser;
    }

    public void setAllocatedByUser(User v) {
        this.allocatedByUser = v;
    }
}

