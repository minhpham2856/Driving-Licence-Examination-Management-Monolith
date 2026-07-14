package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

/**
 * Kết quả chuẩn bị hồ sơ trên bàn thủ tục: profile và cảnh báo ảnh cũ (nếu có).
 */
public class ProcedureProfilePrepareResultDTO {

    private ExamRegistrationDTO profile;
    private String photoStaleMessage;

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
}
