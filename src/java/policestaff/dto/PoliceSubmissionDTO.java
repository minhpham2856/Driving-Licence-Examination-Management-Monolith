package policestaff.dto;

import java.sql.Date;

/** Dòng tổng hợp một danh sách ngày thi dự kiến đã gửi tới CSGT. */
public class PoliceSubmissionDTO {
    private int examDateId;
    private Date examDate;
    private String licenceClass;
    private String policeStatus;
    private int totalCandidates;
    private int pendingCandidates;
    private int approvedCandidates;
    private int rejectedCandidates;

    public int getExamDateId() { return examDateId; }
    public void setExamDateId(int examDateId) { this.examDateId = examDateId; }
    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String licenceClass) { this.licenceClass = licenceClass; }
    public String getPoliceStatus() { return policeStatus; }
    public void setPoliceStatus(String policeStatus) { this.policeStatus = policeStatus; }
    public int getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(int totalCandidates) { this.totalCandidates = totalCandidates; }
    public int getPendingCandidates() { return pendingCandidates; }
    public void setPendingCandidates(int pendingCandidates) { this.pendingCandidates = pendingCandidates; }
    public int getApprovedCandidates() { return approvedCandidates; }
    public void setApprovedCandidates(int approvedCandidates) { this.approvedCandidates = approvedCandidates; }
    public int getRejectedCandidates() { return rejectedCandidates; }
    public void setRejectedCandidates(int rejectedCandidates) { this.rejectedCandidates = rejectedCandidates; }
    public boolean isCompleted() { return "COMPLETED".equalsIgnoreCase(policeStatus); }
}
