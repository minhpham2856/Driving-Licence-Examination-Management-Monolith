package examstaff.dto;

import java.io.File;

/**
 * Kết quả tìm ảnh thí sinh để stream HTTP (BLL → servlet).
 * Mang trạng thái, file ảnh và content-type.
 */
public class CandidatePhotoStreamDTO {

    /** Kết quả tìm file ảnh. */
    public enum Status {
        /** Đã tìm thấy file ảnh. */
        FOUND,
        /** Không có ảnh hợp lệ. */
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
