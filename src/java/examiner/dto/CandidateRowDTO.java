package examiner.dto;

import shared.enums.CandidateStatus;
import shared.enums.Sex;

// Data transfer object for examiner candidate row views.
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
    private String status;
    private String statusLabel;
    private String sexValue;
    private String sexLabel;
    private int correct;
    private int wrong;
    private int unanswered;
    private Integer examScore;
    private Integer scoreTheory;
    private Integer scorePractical;
    private boolean passed;
    private String resultLabel;
    private String vehicleName;
    private int examAreaId;
    private String examAreaName;
    private String avatarClass;
    private String photoImageUrl;
    private String dobRaw;
    private boolean suspended;
    private boolean absent;
    private boolean present;
    private boolean resultPrinted;
    private boolean awaitingSignature;
    private boolean actionEligible;
    private boolean violationEligible;
    private boolean markPresentEligible;
    private String markPresentBlockedReason;
    private boolean undoPresentEligible;
    private boolean wrongInfoEligible;
    private boolean completeEligible;
    private boolean practicalEntryAllowed = true;
    private boolean practicalAttendanceAllowed = true;
    private boolean sectionRequired = true;
    private boolean scoreEntryEligible;
    private boolean active;
    private boolean invoked;

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

    public String getSexValue() {
        return sexValue;
    }

    public void setSexValue(String sexValue) {
        this.sexValue = sexValue;
    }

    public String getSexLabel() {
        return sexLabel;
    }

    public void setSexLabel(String sexLabel) {
        this.sexLabel = sexLabel;
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

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public String getExamAreaName() {
        return examAreaName;
    }

    public void setExamAreaName(String examAreaName) {
        this.examAreaName = examAreaName;
    }

    public String getAvatarClass() {
        return avatarClass;
    }

    public void setAvatarClass(String avatarClass) {
        this.avatarClass = avatarClass;
    }

    public String getPhotoImageUrl() {
        return photoImageUrl;
    }

    public void setPhotoImageUrl(String photoImageUrl) {
        this.photoImageUrl = photoImageUrl;
    }

    public String getDobRaw() {
        return dobRaw;
    }

    public void setDobRaw(String dobRaw) {
        this.dobRaw = dobRaw;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isResultPrinted() {
        return resultPrinted;
    }

    public void setResultPrinted(boolean resultPrinted) {
        this.resultPrinted = resultPrinted;
    }

    public boolean isAwaitingSignature() {
        return awaitingSignature;
    }

    public void setAwaitingSignature(boolean awaitingSignature) {
        this.awaitingSignature = awaitingSignature;
    }

    public boolean isActionEligible() {
        return actionEligible;
    }

    public void setActionEligible(boolean actionEligible) {
        this.actionEligible = actionEligible;
    }

    public boolean isViolationEligible() {
        return violationEligible;
    }

    public void setViolationEligible(boolean violationEligible) {
        this.violationEligible = violationEligible;
    }

    public boolean isMarkPresentEligible() {
        return markPresentEligible;
    }

    public void setMarkPresentEligible(boolean markPresentEligible) {
        this.markPresentEligible = markPresentEligible;
    }

    public String getMarkPresentBlockedReason() {
        return markPresentBlockedReason;
    }

    public void setMarkPresentBlockedReason(String markPresentBlockedReason) {
        this.markPresentBlockedReason = markPresentBlockedReason;
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

    public boolean isCompleteEligible() {
        return completeEligible;
    }

    public void setCompleteEligible(boolean completeEligible) {
        this.completeEligible = completeEligible;
    }

    public boolean isPracticalEntryAllowed() {
        return practicalEntryAllowed;
    }

    public void setPracticalEntryAllowed(boolean practicalEntryAllowed) {
        this.practicalEntryAllowed = practicalEntryAllowed;
    }

    public boolean isPracticalAttendanceAllowed() {
        return practicalAttendanceAllowed;
    }

    public void setPracticalAttendanceAllowed(boolean practicalAttendanceAllowed) {
        this.practicalAttendanceAllowed = practicalAttendanceAllowed;
    }

    public boolean isSectionRequired() {
        return sectionRequired;
    }

    public void setSectionRequired(boolean sectionRequired) {
        this.sectionRequired = sectionRequired;
    }

    public boolean isScoreEntryEligible() {
        return scoreEntryEligible;
    }

    public void setScoreEntryEligible(boolean scoreEntryEligible) {
        this.scoreEntryEligible = scoreEntryEligible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(boolean invoked) {
        this.invoked = invoked;
    }
}
