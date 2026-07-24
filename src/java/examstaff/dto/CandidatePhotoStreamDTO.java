package examstaff.dto;

import java.io.File;

/**
 * Kết quả tìm ảnh thí sinh để stream HTTP (BLL → servlet tải ảnh).
 *
 * Vai trò:
 * Báo FOUND/NOT_FOUND kèm File và content-type; CandidatePhotoServlet ghi bytes response.
 *
 * Ai tạo / tiêu thụ:
 * CandidatePhotoServiceImpl → CandidatePhotoServlet (không JSP).
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

    /** Trạng thái tìm ảnh (mặc định Status.NOT_FOUND). */
    public Status getStatus() {
        return status;
    }

    /** Gán trạng thái tìm ảnh. */
    public void setStatus(Status status) {
        this.status = status;
    }

    /** File ảnh trên đĩa khi Status.FOUND. */
    public File getPhotoFile() {
        return photoFile;
    }

    /** Gán file ảnh cần stream. */
    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }

    /** MIME type (ví dụ image/jpeg) cho header HTTP. */
    public String getContentType() {
        return contentType;
    }

    /** Gán content-type response. */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
