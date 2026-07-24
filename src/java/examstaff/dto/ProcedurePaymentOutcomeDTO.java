package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

/**
 * Kết quả thu lệ phí / thanh toán thủ tục (BLL → Presentation).
 * Mang status nghiệp vụ, profile cập nhật và dữ liệu audit.
 */
public class ProcedurePaymentOutcomeDTO {

    /** Kết quả nghiệp vụ thu lệ phí. */
    public enum Status {
        /** Thanh toán thành công. */
        SUCCESS,
        /** Đã thanh toán trước đó. */
        ALREADY_PAID,
        /** Thiếu ảnh thủ tục — không thu phí. */
        NO_PHOTO,
        /** Ghi thanh toán thất bại. */
        PAYMENT_FAILED,
        /** Không tìm thấy hồ sơ thí sinh. */
        PROFILE_NOT_FOUND
    }

    private Status status = Status.PAYMENT_FAILED;
    private ExamRegistrationDTO profile;
    private List<ExamRegistrationDTO> queue;
    private int boardExamId;
    private String paymentAuditDetail;
    private boolean auditAllocate;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    public List<ExamRegistrationDTO> getQueue() {
        return queue;
    }

    public void setQueue(List<ExamRegistrationDTO> queue) {
        this.queue = queue;
    }

    public int getBoardExamId() {
        return boardExamId;
    }

    public void setBoardExamId(int boardExamId) {
        this.boardExamId = boardExamId;
    }

    public String getPaymentAuditDetail() {
        return paymentAuditDetail;
    }

    public void setPaymentAuditDetail(String paymentAuditDetail) {
        this.paymentAuditDetail = paymentAuditDetail;
    }

    public boolean isAuditAllocate() {
        return auditAllocate;
    }

    public void setAuditAllocate(boolean auditAllocate) {
        this.auditAllocate = auditAllocate;
    }
}
