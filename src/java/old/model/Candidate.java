package model;
import java.sql.Timestamp;
public class Candidate {
    private int candidateId;
    private String candidateNumber;
    private String fullName;
    private Timestamp dateOfBirth;
    private String phoneNumber;
    private boolean sex;
    private String governmentIdNumber;
    private String address;
    private Boolean takeTheory;
    private Boolean takeLayout;
    private int takeNo;
    private String reasonForTaking;
    private String photoImageUrl;
    private boolean isAbsent;
    private boolean isSuspended;
    public Candidate() {
    }
    public Candidate(int candidateId, String candidateNumber, String fullName, Timestamp dateOfBirth,
            String phoneNumber, boolean sex, String governmentIdNumber, String address,
            Boolean takeTheory, Boolean takeLayout,
            int takeNo, String reasonForTaking, String photoImageUrl,
            boolean isAbsent, boolean isSuspended) {
        this.candidateId = candidateId;
        this.candidateNumber = candidateNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.governmentIdNumber = governmentIdNumber;
        this.address = address;
        this.takeTheory = takeTheory;
        this.takeLayout = takeLayout;
        this.takeNo = takeNo;
        this.reasonForTaking = reasonForTaking;
        this.photoImageUrl = photoImageUrl;
        this.isAbsent = isAbsent;
        this.isSuspended = isSuspended;
    }
    public int getCandidateId() {
        return candidateId;
    }
    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
    public String getCandidateNumber() {
        return candidateNumber;
    }
    public void setCandidateNumber(String candidateNumber) {
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
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public boolean isSex() {
        return sex;
    }
    public void setSex(boolean sex) {
        this.sex = sex;
    }
    public String getGovernmentIdNumber() {
        return governmentIdNumber;
    }
    public void setGovernmentIdNumber(String governmentIdNumber) {
        this.governmentIdNumber = governmentIdNumber;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Boolean getTakeTheory() {
        return takeTheory;
    }
    public void setTakeTheory(Boolean takeTheory) {
        this.takeTheory = takeTheory;
    }
    public Boolean getTakeLayout() {
        return takeLayout;
    }
    public void setTakeLayout(Boolean takeLayout) {
        this.takeLayout = takeLayout;
    }
    public int getTakeNo() {
        return takeNo;
    }
    public void setTakeNo(int takeNo) {
        this.takeNo = takeNo;
    }
    public String getReasonForTaking() {
        return reasonForTaking;
    }
    public void setReasonForTaking(String reasonForTaking) {
        this.reasonForTaking = reasonForTaking;
    }
    public String getPhotoImageUrl() {
        return photoImageUrl;
    }
    public void setPhotoImageUrl(String photoImageUrl) {
        this.photoImageUrl = photoImageUrl;
    }
    public boolean isAbsent() {
        return isAbsent;
    }
    public void setAbsent(boolean isAbsent) {
        this.isAbsent = isAbsent;
    }
    public boolean isSuspended() {
        return isSuspended;
    }
    public void setSuspended(boolean isSuspended) {
        this.isSuspended = isSuspended;
    }
}
