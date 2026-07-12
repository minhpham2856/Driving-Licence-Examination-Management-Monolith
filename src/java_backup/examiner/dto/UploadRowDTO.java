package examiner.dto;

import java.sql.*;

public class UploadRowDTO {

    private int id;
    private int personId;
    private int examId;
    private String fullName;
    private String govIdNo;
    private String licenseCode;
    private String phoneNo;
    private String email;
    private String registrationType;
    private boolean isPaymentCompleted;
    private boolean isPresent;
    private Date dateOfBirth;
    private int candidateNo;
    private boolean isDuplicate;
    private boolean isInvalid;
    private String validationMessage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
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

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
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

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public boolean isPaymentCompleted() {
        return isPaymentCompleted;
    }

    public void setIsPaymentCompleted(boolean paymentCompleted) {
        isPaymentCompleted = paymentCompleted;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setIsPresent(boolean present) {
        isPresent = present;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getCandidateNo() {
        return candidateNo;
    }

    public void setCandidateNo(int candidateNo) {
        this.candidateNo = candidateNo;
    }

    // Display value for the preview table: "Hạng-SBD" (e.g. B2-0145).
    public String getSbd() {
        if (licenseCode == null || licenseCode.isEmpty()) {
            return String.format("%04d", candidateNo);
        }
        return licenseCode + "-" + String.format("%04d", candidateNo);
    }

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
}
