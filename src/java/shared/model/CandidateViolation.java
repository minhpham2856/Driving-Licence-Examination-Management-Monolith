package shared.model;

import java.sql.Timestamp;

public class CandidateViolation {
    private int candidateViolationId;
    private int examEnrollmentSectionId;
    private String reason;
    private String details;
    private String evidenceUrl;
    private int createdBy;
    private Timestamp createdAt;
    private ExamEnrollmentSection examEnrollmentSection;
    private User createdByUser;

    public int getCandidateViolationId() { return candidateViolationId; }
    public void setCandidateViolationId(int value) { candidateViolationId = value; }
    public int getExamEnrollmentSectionId() { return examEnrollmentSectionId; }
    public void setExamEnrollmentSectionId(int value) { examEnrollmentSectionId = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public String getDetails() { return details; }
    public void setDetails(String value) { details = value; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String value) { evidenceUrl = value; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int value) { createdBy = value; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp value) { createdAt = value; }
    public ExamEnrollmentSection getExamEnrollmentSection() { return examEnrollmentSection; }
    public void setExamEnrollmentSection(ExamEnrollmentSection value) { examEnrollmentSection = value; }
    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User value) { createdByUser = value; }
}
