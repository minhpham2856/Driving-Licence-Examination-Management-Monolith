package dto;

import enums.CandidateStatus;

public class CandidateRowDTO {

    private int sbd;
    private int enrollmentId;
    private String fullName;
    private String dob;
    private String dobRaw;
    private String governmentId;
    private String address;
    private String phoneNo;
    private String sex;
    private String sexValue;
    private String email;
    private String licenceClass;
    private String reasonForTaking;
    private String examDate;
    private CandidateStatus sectionStatus;
    private String status;
    private String statusLabel;
    private boolean absent;
    private boolean suspended;
    private boolean callEligible;
    private int correct;
    private int wrong;
    private int unanswered;
    private Object examScore;
    private Object scoreTheory;
    private Object scorePractical;
    private Object scoreOnRoad;
    private boolean passed;
    private String resultLabel;
    private String vehicleName;
    private boolean awaitingSignature;
    private boolean violationEligible;
    private boolean completeEligible;
    private boolean markPresentEligible;
    private boolean undoPresentEligible;
    private boolean wrongInfoEligible;
    private boolean present;
    private boolean inProcedure;
    private String statusKey;
    private String avatarClass;
    private String username;
    private String cccd;
    private String licenseClass;
    private String caLabel;

    public int getSbd() {
        return sbd;
    }

    public void setSbd(int sbd) {
        this.sbd = sbd;
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

    public String getDobRaw() {
        return dobRaw;
    }

    public void setDobRaw(String dobRaw) {
        this.dobRaw = dobRaw;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSexValue() {
        return sexValue;
    }

    public void setSexValue(String sexValue) {
        this.sexValue = sexValue;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
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

    public boolean isCallEligible() {
        return callEligible;
    }

    public void setCallEligible(boolean callEligible) {
        this.callEligible = callEligible;
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

    public Object getExamScore() {
        return examScore;
    }

    public void setExamScore(Object examScore) {
        this.examScore = examScore;
    }

    public Object getScoreTheory() {
        return scoreTheory;
    }

    public void setScoreTheory(Object scoreTheory) {
        this.scoreTheory = scoreTheory;
    }

    public Object getScorePractical() {
        return scorePractical;
    }

    public void setScorePractical(Object scorePractical) {
        this.scorePractical = scorePractical;
    }

    public Object getScoreOnRoad() {
        return scoreOnRoad;
    }

    public void setScoreOnRoad(Object scoreOnRoad) {
        this.scoreOnRoad = scoreOnRoad;
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

    public boolean isAwaitingSignature() {
        return awaitingSignature;
    }

    public void setAwaitingSignature(boolean awaitingSignature) {
        this.awaitingSignature = awaitingSignature;
    }

    public boolean isViolationEligible() {
        return violationEligible;
    }

    public void setViolationEligible(boolean violationEligible) {
        this.violationEligible = violationEligible;
    }

    public boolean isCompleteEligible() {
        return completeEligible;
    }

    public void setCompleteEligible(boolean completeEligible) {
        this.completeEligible = completeEligible;
    }

    public boolean isMarkPresentEligible() {
        return markPresentEligible;
    }

    public void setMarkPresentEligible(boolean markPresentEligible) {
        this.markPresentEligible = markPresentEligible;
    }

    public boolean isUndoPresentEligible() {
        return undoPresentEligible;
    }

    public void setUndoPresentEligible(boolean undoPresentEligible) {
        this.undoPresentEligible = undoPresentEligible;
    }

    public boolean isWrongInfoEligible() {
        return wrongInfoEligible;
    }

    public void setWrongInfoEligible(boolean wrongInfoEligible) {
        this.wrongInfoEligible = wrongInfoEligible;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isInProcedure() {
        return inProcedure;
    }

    public void setInProcedure(boolean inProcedure) {
        this.inProcedure = inProcedure;
    }

    public String getStatusKey() {
        return statusKey;
    }

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }

    public String getAvatarClass() {
        return avatarClass;
    }

    public void setAvatarClass(String avatarClass) {
        this.avatarClass = avatarClass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getLicenseClass() {
        return licenseClass;
    }

    public void setLicenseClass(String licenseClass) {
        this.licenseClass = licenseClass;
    }

    public String getCaLabel() {
        return caLabel;
    }

    public void setCaLabel(String caLabel) {
        this.caLabel = caLabel;
    }
}
