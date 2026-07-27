package managingstaff.dto;

import java.sql.Date;

/**
 * Candidate row parsed from the official CSV before an exam is created.
 */
public class ExamRegistrationDTO {

    private int candidateNo;
    private String registrationType;
    private boolean paymentCompleted;
    private boolean present;
    private String fullName;
    private String govIdNo;
    private Date dateOfBirth;
    private String phoneNo;
    private String email;
    private String licenseCode;
    private String sourceUnitCode;
    private String sourceUnitName;
    private String examParticipationType;
    private Integer examRegistrationId;
    private boolean invalid;
    private String validationMessage;
    private boolean duplicate;

    public String getSbd() {
        return candidateNo <= 0 ? "000" : String.format("%03d", candidateNo);
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

    public void setIsPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    public boolean isPresent() {
        return present;
    }

    public void setIsPresent(boolean present) {
        this.present = present;
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

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getSourceUnitCode() { return sourceUnitCode; }
    public void setSourceUnitCode(String value) { sourceUnitCode = value; }
    public String getSourceUnitName() { return sourceUnitName; }
    public void setSourceUnitName(String value) { sourceUnitName = value; }
    public String getExamParticipationType() { return examParticipationType; }
    public void setExamParticipationType(String value) { examParticipationType = value; }
    public String getExamParticipationLabel() {
        return "PRACTICAL_ONLY".equalsIgnoreCase(examParticipationType)
                ? "Chỉ thi thực hành" : "Lý thuyết và thực hành";
    }
    public Integer getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(Integer value) { examRegistrationId = value; }
    public boolean isInternalCandidate() { return examRegistrationId != null; }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }
}
