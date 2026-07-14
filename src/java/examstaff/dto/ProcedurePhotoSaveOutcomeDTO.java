package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

/**
 * Kết quả lưu ảnh thủ tục thí sinh: status, thông báo, đường dẫn và profile.
 */
public class ProcedurePhotoSaveOutcomeDTO {

    /** Kết quả lưu ảnh thủ tục. */
    public enum Status {
        /** Lưu ảnh thành công. */
        SUCCESS,
        /** Không tìm thấy thí sinh. */
        CANDIDATE_NOT_FOUND,
        /** Ảnh không hợp lệ / sai định dạng. */
        INVALID_IMAGE,
        /** Lỗi không xác định khi lưu. */
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
