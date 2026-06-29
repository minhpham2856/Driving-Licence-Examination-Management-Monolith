package dto;

public class CandidateProfileDTO {
    private int candidateId;
    private String candidateNumber;
    private String fullName;
    private String governmentIdNumber;
    private boolean isAbsent;
    private boolean isSuspended;

    
    private String photoImageUrl;

    public String getPhotoImageUrl() {
        return photoImageUrl;
    }

    public void setPhotoImageUrl(String photoImageUrl) {
        this.photoImageUrl = photoImageUrl;
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

    public String getGovernmentIdNumber() {
        return governmentIdNumber;
    }

    public void setGovernmentIdNumber(String governmentIdNumber) {
        this.governmentIdNumber = governmentIdNumber;
    }

    public boolean isAbsent() {
        return isAbsent;
    }

    public void setAbsent(boolean absent) {
        isAbsent = absent;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean suspended) {
        isSuspended = suspended;
    }
}

