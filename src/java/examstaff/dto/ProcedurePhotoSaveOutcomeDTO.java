package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

public class ProcedurePhotoSaveOutcomeDTO {

    public enum Status {
        SUCCESS,
        CANDIDATE_NOT_FOUND,
        INVALID_IMAGE,
        ERROR
    }

    private Status status = Status.ERROR;
    private String message;
    private String photoPath;
    private ExamRegistrationDTO profile;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }
}
