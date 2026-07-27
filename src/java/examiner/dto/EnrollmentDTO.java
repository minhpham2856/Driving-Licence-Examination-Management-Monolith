package examiner.dto;

import java.sql.Timestamp;
import shared.enums.CandidateStatus;

// Enrollment view for examiner screens with flat scalar fields.
public class EnrollmentDTO {

    private int candidateId;
    private int examEnrollmentId;
    private int examId;
    private int candidateNumber;
    private String fullName;
    private Timestamp dateOfBirth;
    private String governmentIdNumber;
    private String phoneNumber;
    private String address;
    private String email;
    private String reasonForTaking;
    private boolean sex;
    private String photoImageUrl;
    private boolean takeTheory;
    private boolean takeLayout;
    private boolean absent;
    private boolean suspended;
    private boolean present;
    private boolean paymentCompleted;
    private boolean validCapturedPhoto;
    private CandidateStatus sectionStatus = CandidateStatus.NOT_STARTED;
    private boolean resultPrinted;
    private int allocatedAreaId;
    private String allocatedAreaName;
    private String notes;
    private Integer theoryScore;
    private Integer practicalScore;
    private String theoryPassed;
    private String practicalPassed;
    private Integer examDeviceId;

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(int candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGovernmentIdNumber() {
        return governmentIdNumber;
    }

    public void setGovernmentIdNumber(String governmentIdNumber) {
        this.governmentIdNumber = governmentIdNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReasonForTaking() {
        return reasonForTaking;
    }

    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public String getPhotoImageUrl() {
        return photoImageUrl;
    }

    public void setPhotoImageUrl(String photoImageUrl) {
        this.photoImageUrl = photoImageUrl;
    }

    public boolean isTakeTheory() {
        return takeTheory;
    }

    public void setTakeTheory(boolean takeTheory) {
        this.takeTheory = takeTheory;
    }

    public boolean isTakeLayout() {
        return takeLayout;
    }

    public void setTakeLayout(boolean takeLayout) {
        this.takeLayout = takeLayout;
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

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public void setPaymentCompleted(boolean paymentCompleted) {
        this.paymentCompleted = paymentCompleted;
    }

    public boolean isValidCapturedPhoto() {
        return validCapturedPhoto;
    }

    public void setValidCapturedPhoto(boolean validCapturedPhoto) {
        this.validCapturedPhoto = validCapturedPhoto;
    }

    public CandidateStatus getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(CandidateStatus sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    public boolean isResultPrinted() {
        return resultPrinted;
    }

    public void setResultPrinted(boolean resultPrinted) {
        this.resultPrinted = resultPrinted;
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

    public Integer getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(Integer examDeviceId) {
        this.examDeviceId = examDeviceId;
    }
}
