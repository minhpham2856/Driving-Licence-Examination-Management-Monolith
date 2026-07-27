package policestaff.dto;

import java.sql.Date;

public class OfficialExamCandidateDTO {
    private int id;
    private int examDateId;
    private Integer examRegistrationId;
    private Integer registrationDateId;
    private String candidateNumber;
    private String fullName;
    private Date dateOfBirth;
    private String governmentIdNumber;
    private String licenceClass;
    private String phoneNumber;
    private String email;
    private String sourceUnitCode;
    private String sourceUnitName;
    private String examParticipationType;

    public int getId() { return id; }
    public void setId(int value) { id = value; }
    public int getExamDateId() { return examDateId; }
    public void setExamDateId(int value) { examDateId = value; }
    public Integer getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(Integer value) { examRegistrationId = value; }
    public Integer getRegistrationDateId() { return registrationDateId; }
    public void setRegistrationDateId(Integer value) { registrationDateId = value; }
    public String getCandidateNumber() { return candidateNumber; }
    public void setCandidateNumber(String value) { candidateNumber = value; }
    public String getFullName() { return fullName; }
    public void setFullName(String value) { fullName = value; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date value) { dateOfBirth = value; }
    public String getGovernmentIdNumber() { return governmentIdNumber; }
    public void setGovernmentIdNumber(String value) { governmentIdNumber = value; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String value) { licenceClass = value; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String value) { phoneNumber = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
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
    public boolean isInternalCandidate() { return examRegistrationId != null; }
}
