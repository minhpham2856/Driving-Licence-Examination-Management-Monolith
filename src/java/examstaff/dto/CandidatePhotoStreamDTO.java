package examstaff.dto;

import java.io.File;

public class CandidatePhotoStreamDTO {

    public enum Status {
        FOUND,
        NOT_FOUND
    }

    private Status status = Status.NOT_FOUND;
    private File photoFile;
    private String contentType;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
