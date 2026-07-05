package dto;
import java.sql.*;
import model.ExamEnrollment;
import enums.CandidateStatus;
public class CandidateEnrollmentDTO {
    private CandidateProfileDTO candidate;
    private ExamEnrollment enrollment;
    public CandidateEnrollmentDTO() {}
    public CandidateEnrollmentDTO(CandidateProfileDTO candidate, ExamEnrollment enrollment) {
        this.candidate = candidate;
        this.enrollment = enrollment;
    }
    public CandidateProfileDTO getCandidate() { return candidate; }
    public void setCandidate(CandidateProfileDTO candidate) { this.candidate = candidate; }
    public ExamEnrollment getEnrollment() { return enrollment; }
    public void setEnrollment(ExamEnrollment enrollment) { this.enrollment = enrollment; }
    public int getId() { return candidate != null ? candidate.getCandidateId() : 0; }
    public String getCandidateName() { return candidate != null ? candidate.getFullName() : ""; }
    public Date getDob() {
        return dateOfBirth != null ? new Date(dateOfBirth.getTime()) : null;
    }
    public int getSbd() {
        return candidate != null ? candidate.getCandidateNumber() : 0;
    }
    public int getCandidateNo() {
        return getSbd();
    }
    public String getFullName() { return candidate != null ? candidate.getFullName() : ""; }
    public String getGovIdNo() { return candidate != null ? candidate.getGovernmentIdNumber() : ""; }
    public boolean isAbsent() { return candidate != null && candidate.isAbsent(); }
    public void setAbsent(boolean absent) { if (candidate != null) candidate.setAbsent(absent); }
    public boolean isSuspended() { return candidate != null && candidate.isSuspended(); }
    public String getSectionStatus() {
        return enrollment != null ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED.getValue();
    }
    public void setSectionStatus(String status) { if (enrollment != null) enrollment.setSectionStatus(status); }
    public boolean isSignaturePrinted() { return enrollment != null && enrollment.isSignaturePrinted(); }
    public int getExamSessionId() { return enrollment != null ? enrollment.getSessionId() : 0; }
    public boolean isPaymentCompleted() { return true; }
    public boolean isValidCapturedPhoto() { return candidate != null && candidate.getPhotoImageUrl() != null && !candidate.getPhotoImageUrl().isEmpty(); }
    public int getAllocatedAreaId() { return 0; }
    public void setAllocatedAreaId(int id) {}
    public void setAllocatedAreaName(String name) {}
    public void setNotes(String n) {}
    public Integer getTheoryScore() { return 0; }
    public Integer getPracticalScore() { return 0; }
    public Integer getRoadTestScore() { return 0; }
    public boolean isPresent() { return candidate != null && !candidate.isAbsent(); }
    public void setTheoryPassed(String s) {}
    public void setPracticalPassed(String s) {}
    public void setTheoryScore(Integer i) {}
    public void setPracticalScore(Integer i) {}
    public void setIsPaymentCompleted(boolean b) {}
    public void setIsPresent(boolean b) { if (candidate != null) candidate.setAbsent(!b); }
    public void setValidCapturedPhoto(boolean b) {}
    public void setRoadTestPassed(String s) {}
    public void setRoadTestScore(Integer i) {}
    public void setPhotoUrl(String s) { if(candidate != null) candidate.setPhotoImageUrl(s); }
    private Timestamp dateOfBirth;
    private String phoneNo;
    private String address;
    private String reasonForTaking;
    private boolean sex;
    public void setDateOfBirth(Timestamp dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo != null ? phoneNo : ""; }
    public void setAddress(String address) { this.address = address != null ? address : ""; }
    public void setReasonForTaking(String reasonForTaking) { this.reasonForTaking = reasonForTaking != null ? reasonForTaking : ""; }
    public void setSex(boolean sex) { this.sex = sex; }
    public Timestamp getDateOfBirth() { return dateOfBirth; }
    public String getEmail() { return ""; }
    public String getPhoneNo() { return phoneNo != null ? phoneNo : ""; }
    public String getAddress() { return address != null ? address : ""; }
    public boolean isSex() { return sex; }
    public String getReasonForTaking() { return reasonForTaking != null ? reasonForTaking : ""; }
}
