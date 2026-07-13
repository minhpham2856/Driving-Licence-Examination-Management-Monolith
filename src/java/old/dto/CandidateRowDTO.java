package dto;

import enums.CandidateStatus;
import enums.Sex;

public class CandidateRowDTO {

    private int candidateNumber;
    private int enrollmentId;
    private String fullName;
    private String dob;
    private String governmentId;
    private String address;
    private String phoneNo;
    private Sex sex;
    private String email;
    private String licenceClass;
    private String reasonForTaking;
    private String examDate;
    private CandidateStatus sectionStatus;
    private int correct;
    private int wrong;
    private int unanswered;
    private Integer examScore;
    private Integer scoreTheory;
    private Integer scorePractical;
    private boolean passed;
    private String resultLabel;
    private String vehicleName;
    private String avatarClass;

    public int getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(int candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public String getReasonForTaking() {
        return reasonForTaking;
    }

    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public CandidateStatus getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(CandidateStatus sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getWrong() {
        return wrong;
    }

    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

    public int getUnanswered() {
        return unanswered;
    }

    public void setUnanswered(int unanswered) {
        this.unanswered = unanswered;
    }

    public Integer getExamScore() {
        return examScore;
    }

    public void setExamScore(Integer examScore) {
        this.examScore = examScore;
    }

    public Integer getScoreTheory() {
        return scoreTheory;
    }

    public void setScoreTheory(Integer scoreTheory) {
        this.scoreTheory = scoreTheory;
    }

    public Integer getScorePractical() {
        return scorePractical;
    }

    public void setScorePractical(Integer scorePractical) {
        this.scorePractical = scorePractical;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getAvatarClass() {
        return avatarClass;
    }

    public void setAvatarClass(String avatarClass) {
        this.avatarClass = avatarClass;
    }
}
