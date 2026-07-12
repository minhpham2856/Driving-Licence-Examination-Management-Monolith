package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

public class ProcedureProfilePrepareResultDTO {

    private ExamRegistrationDTO profile;
    private String photoStaleMessage;
    private boolean presentUpdated;

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    public String getPhotoStaleMessage() {
        return photoStaleMessage;
    }

    public void setPhotoStaleMessage(String photoStaleMessage) {
        this.photoStaleMessage = photoStaleMessage;
    }

    public boolean isPresentUpdated() {
        return presentUpdated;
    }

    public void setPresentUpdated(boolean presentUpdated) {
        this.presentUpdated = presentUpdated;
    }
}
