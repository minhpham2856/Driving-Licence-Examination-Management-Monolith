package examiner.dto;

import java.sql.*;
import shared.model.ExamEnrollment;
import shared.enums.CandidateStatus;

public class EnrollmentDTO {

    private CandidateProfileDTO candidate;
    private ExamEnrollment enrollment;

    private Integer theoryScore;
    private Integer practicalScore;
    private String theoryPassed;
    private String practicalPassed;
    private int allocatedAreaId;
    private String allocatedAreaName;
    private String notes;
    private boolean paymentCompleted;
    private boolean present;
    private boolean validCapturedPhoto;

    private Timestamp dateOfBirth;
    private String phoneNo;
    private String address;
    private String reasonForTaking;
    private boolean sex;
    private String email = "";

    public EnrollmentDTO() {
    }

    public EnrollmentDTO(CandidateProfileDTO candidate, ExamEnrollment enrollment) {
        this.candidate = candidate;
        this.enrollment = enrollment;
    }

    public CandidateProfileDTO getCandidate() {
        return candidate;
    }

    public void setCandidate(CandidateProfileDTO candidate) {
        this.candidate = candidate;
    }

    public ExamEnrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(ExamEnrollment enrollment) {
        this.enrollment = enrollment;
    }

    public int getId() {
        return candidate != null ? candidate.getCandidateId() : 0;
    }

    public String getCandidateName() {
        return candidate != null ? candidate.getFullName() : "";
    }

    public Date getDob() {
        return dateOfBirth != null ? new Date(dateOfBirth.getTime()) : null;
    }

    public int getCandidateNumber() {
        return candidate != null ? candidate.getCandidateNumber() : 0;
    }

    public int getCandidateNo() {
        return getCandidateNumber();
    }

    public String getFullName() {
        return candidate != null ? candidate.getFullName() : "";
    }

    public String getGovIdNo() {
        return candidate != null ? candidate.getGovernmentIdNumber() : "";
    }

    public boolean isAbsent() {
        return candidate != null && candidate.isAbsent();
    }

    public void setAbsent(boolean absent) {
        if (candidate != null) {
            candidate.setAbsent(absent);
        }
    }

    public boolean isSuspended() {
        return candidate != null && candidate.isSuspended();
    }

    public String getSectionStatus() {
        return enrollment != null ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED.getValue();
    }

    public void setSectionStatus(String status) {
        if (enrollment != null) {
            enrollment.setSectionStatus(status);
        }
    }

    public boolean isSignaturePrinted() {
        return enrollment != null && enrollment.isSignaturePrinted();
    }

    public int getExamId() {
        return enrollment != null ? enrollment.getExamId() : 0;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public void setIsPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    public boolean isValidCapturedPhoto() {
        return validCapturedPhoto || (candidate != null && candidate.getPhotoImageUrl() != null && !candidate.getPhotoImageUrl().isEmpty());
    }

    public void setValidCapturedPhoto(boolean validCapturedPhoto) {
        this.validCapturedPhoto = validCapturedPhoto;
    }

    public int getAllocatedAreaId() {
        return allocatedAreaId;
    }

    public void setAllocatedAreaId(int allocatedAreaId) {
        this.allocatedAreaId = allocatedAreaId;
    }

    public String getAllocatedAreaName() {
        return allocatedAreaName;
    }

    public void setAllocatedAreaName(String allocatedAreaName) {
        this.allocatedAreaName = allocatedAreaName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    public boolean isPresent() {
        return present || (candidate != null && !candidate.isAbsent());
    }

    public void setIsPresent(boolean present) {
        this.present = present;
        if (candidate != null) {
            candidate.setAbsent(!present);
        }
    }

    public String getTheoryPassed() {
        return theoryPassed;
    }

    public void setTheoryPassed(String theoryPassed) {
        this.theoryPassed = theoryPassed;
    }

    public String getPracticalPassed() {
        return practicalPassed;
    }

    public void setPracticalPassed(String practicalPassed) {
        this.practicalPassed = practicalPassed;
    }

    public void setPhotoUrl(String s) {
        if (candidate != null) {
            candidate.setPhotoImageUrl(s);
        }
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo != null ? phoneNo : "";
    }

    public void setAddress(String address) {
        this.address = address != null ? address : "";
    }

    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking != null ? reasonForTaking : "";
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo != null ? phoneNo : "";
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public boolean isSex() {
        return sex;
    }

    public String getReasonForTaking() {
        return reasonForTaking != null ? reasonForTaking : "";
    }
}

