package Models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Thí sinh trong danh sách Công an import (SBD chính thức theo đợt thi).
 */
public class Candidate {

    private int id;
    private int examSessionId;
    private Integer personId;
    private Integer examRegistrationId;
    private String candidateNo;
    private String govIdNo;
    private String fullName;
    private Date dateOfBirth;
    private String licenseCode;
    private Timestamp importedAt;
    private Integer importedBy;

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

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public Integer getExamRegistrationId() {
        return examRegistrationId;
    }

    public void setExamRegistrationId(Integer examRegistrationId) {
        this.examRegistrationId = examRegistrationId;
    }

    public String getCandidateNo() {
        return candidateNo;
    }

    public void setCandidateNo(String candidateNo) {
        this.candidateNo = candidateNo;
    }

    public String getGovIdNo() {
        return govIdNo;
    }

    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public Timestamp getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Timestamp importedAt) {
        this.importedAt = importedAt;
    }

    public Integer getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(Integer importedBy) {
        this.importedBy = importedBy;
    }
}
