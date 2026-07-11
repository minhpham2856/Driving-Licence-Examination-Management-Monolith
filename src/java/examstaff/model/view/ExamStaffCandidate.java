package examstaff.model.view;

import java.sql.Date;
import java.sql.Timestamp;

/** Read model hợp nhất Candidate + Enrollment + kết quả thi (SELECT JOIN). */
public class ExamStaffCandidate {

    private int candidateId;
    private int examSessionId;
    private int examEnrollmentId;
    private int candidateNo;
    private String registrationType;
    private boolean paymentCompleted;
    private boolean present;
    private boolean absent;
    private boolean suspended;
    private Timestamp presentMarkedAt;
    private String notes;
    private String fullName;
    private String govIdNo;
    private Date dateOfBirth;
    private boolean male;
    private String phoneNo;
    private String email;
    private String photoUrl;
    private String licenseCode;
    private String computerCode;
    private String address;
    private String reasonForTaking;
    private Boolean takeTheory;
    private Boolean takePractical;
    private Date examDate;
    private String sectionStatus;
    private boolean signaturePrinted;
    private Integer allocatedAreaId;
    private String allocatedAreaName;
    private Integer practicalAllocatedAreaId;
    private String practicalAllocatedAreaName;
    private Integer theoryScore;
    private Integer practicalScore;

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getExamId() {
        return examSessionId;
    }

    public void setExamId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getCandidateNo() {
        return candidateNo;
    }

    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public void setPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Timestamp getPresentMarkedAt() {
        return presentMarkedAt;
    }

    public void setPresentMarkedAt(Timestamp presentMarkedAt) {
        this.presentMarkedAt = presentMarkedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGovIdNo() {
        return govIdNo;
    }

    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getComputerCode() {
        return computerCode;
    }

    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReasonForTaking() {
        return reasonForTaking;
    }

    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking;
    }

    public Boolean getTakeTheory() {
        return takeTheory;
    }

    public void setTakeTheory(Boolean takeTheory) {
        this.takeTheory = takeTheory;
    }

    public Boolean getTakePractical() {
        return takePractical;
    }

    public void setTakePractical(Boolean takePractical) {
        this.takePractical = takePractical;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(String sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    public boolean isSignaturePrinted() {
        return signaturePrinted;
    }

    public void setSignaturePrinted(boolean signaturePrinted) {
        this.signaturePrinted = signaturePrinted;
    }

    public Integer getAllocatedAreaId() {
        return allocatedAreaId;
    }

    public void setAllocatedAreaId(Integer allocatedAreaId) {
        this.allocatedAreaId = allocatedAreaId;
    }

    public String getAllocatedAreaName() {
        return allocatedAreaName;
    }

    public void setAllocatedAreaName(String allocatedAreaName) {
        this.allocatedAreaName = allocatedAreaName;
    }

    public Integer getPracticalAllocatedAreaId() {
        return practicalAllocatedAreaId;
    }

    public void setPracticalAllocatedAreaId(Integer practicalAllocatedAreaId) {
        this.practicalAllocatedAreaId = practicalAllocatedAreaId;
    }

    public String getPracticalAllocatedAreaName() {
        return practicalAllocatedAreaName;
    }

    public void setPracticalAllocatedAreaName(String practicalAllocatedAreaName) {
        this.practicalAllocatedAreaName = practicalAllocatedAreaName;
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

}
