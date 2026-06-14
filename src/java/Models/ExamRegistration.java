package Models;

import java.sql.Timestamp;

public class ExamRegistration {
    private int id;
    private int examSessionId;
    private int personId;
    private int candidateNo;
    private String registrationType; // 'PreRegistered', 'WalkIn'
    private boolean isPaymentCompleted;
    private boolean isPresent;
    private boolean absent;
    private boolean suspended;
    private Timestamp presentMarkedAt;
    private String notes;

    // Helper fields joined from Person
    private String fullName;
    private String govIdNo;
    private java.sql.Date dateOfBirth;
    private boolean gender;
    private String phoneNo;
    private String email;
    private String photoUrl;

    // Helper fields from allocation/pipeline
    private String computerCode;
    private String deviceCode;
    private String theoryPassed = "none"; // 'none', 'passed', 'failed'
    private String practicalPassed = "none";
    private String roadTestPassed = "none"; // 'none', 'passed', 'failed' — chỉ áp dụng B1/B2/C/D/E/F
    private Integer theoryScore;
    private Integer practicalScore;
    private Integer roadTestScore;
    private String licenseCode; // Hạng thi (A1, B2)

    private Integer allocatedAreaId;
    private String allocatedAreaName;
    private boolean isCalled;
    private boolean validCapturedPhoto;
    private String address;
    private String reasonForTaking;
    private java.sql.Date examDate;
    private String sectionStatus = "Pending";
    private boolean signaturePrinted;


    public ExamRegistration() {
    }

    public ExamRegistration(int id, int examSessionId, int personId, int candidateNo, String registrationType, boolean isPaymentCompleted, boolean isPresent, Timestamp presentMarkedAt, String notes) {
        this.id = id;
        this.examSessionId = examSessionId;
        this.personId = personId;
        this.candidateNo = candidateNo;
        this.registrationType = registrationType;
        this.isPaymentCompleted = isPaymentCompleted;
        this.isPresent = isPresent;
        this.presentMarkedAt = presentMarkedAt;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
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

    public boolean isIsPaymentCompleted() {
        return isPaymentCompleted;
    }

    public boolean isPaymentCompleted() {
        return isPaymentCompleted;
    }

    public void setIsPaymentCompleted(boolean isPaymentCompleted) {
        this.isPaymentCompleted = isPaymentCompleted;
    }

    public boolean isIsPresent() {
        return isPresent;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
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

    public java.sql.Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(java.sql.Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
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

    public String getComputerCode() {
        return computerCode;
    }

    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
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

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }
    
    public String getRoadTestPassed() {
        return roadTestPassed;
    }

    public void setRoadTestPassed(String roadTestPassed) {
        this.roadTestPassed = roadTestPassed;
    }

    public Integer getRoadTestScore() {
        return roadTestScore;
    }

    public void setRoadTestScore(Integer roadTestScore) {
        this.roadTestScore = roadTestScore;
    }

    /** Trả về true nếu hạng bằng yêu cầu thi đường trường (B1, B2, C, D, E, F) */
    public boolean isRequiresRoadTest() {
        if (licenseCode == null) return false;
        String lc = licenseCode.toUpperCase().trim();
        return lc.equals("B1") || lc.equals("B2") || lc.equals("C")
            || lc.equals("D") || lc.equals("E") || lc.equals("F")
            || lc.equals("C1") || lc.equals("D1") || lc.equals("D2");
    }

    // Convenience Getters for seamless JSP EL transition
    public String getSbd() {
        if (licenseCode == null) return "XX-0000";
        return licenseCode + "-" + String.format("%04d", candidateNo);
    }

    public String getName() {
        return fullName;
    }

    public String getClazz() {
        return licenseCode;
    }

    public java.sql.Date getDob() {
        return dateOfBirth;
    }

    public String getCccd() {
        return govIdNo;
    }

    public String getPhone() {
        return phoneNo;
    }

    public String getIsPresentStr() {
        return isPresent ? "true" : "false";
    }

    public String getPaymentCompletedStr() {
        return isPaymentCompleted ? "true" : "false";
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

    private boolean isDuplicate;
    private boolean isInvalid;
    private String validationMessage;
    private String allocatedShift = "Kíp 1"; // Default kíp thi

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getAllocatedShift() {
        return allocatedShift;
    }

    public void setAllocatedShift(String allocatedShift) {
        this.allocatedShift = allocatedShift;
    }

    public boolean isCalled() {
        return isCalled;
    }

    public boolean getIsCalled() {
        return isCalled;
    }

    public void setCalled(boolean isCalled) {
        this.isCalled = isCalled;
    }

    public boolean isValidCapturedPhoto() {
        return validCapturedPhoto;
    }

    public void setValidCapturedPhoto(boolean validCapturedPhoto) {
        this.validCapturedPhoto = validCapturedPhoto;
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

    public java.sql.Date getExamDate() {
        return examDate;
    }

    public void setExamDate(java.sql.Date examDate) {
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
}
