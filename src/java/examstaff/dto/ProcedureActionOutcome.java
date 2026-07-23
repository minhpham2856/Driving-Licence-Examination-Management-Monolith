package examstaff.dto;

import java.util.List;

/**
 * Kết quả thao tác wizard thủ tục tại bàn (chụp ảnh / thu lệ phí / reset) từ BLL → Presentation.
 *
 * Vai trò trong luồng examstaff:
 * Sau khi staff thao tác trên desk (include {@code procedure.jsp} trong {@code candidatecall.jsp}),
 * workflow trả outcome gồm status ảnh/thanh toán, profile cập nhật, queue, và cờ audit.
 * {@code ProcedureServlet} đọc các field để set attribute lỗi/thành công và refresh hồ sơ / board.
 *
 * Ai tạo:
 * {@code ProcedureWorkflowServiceImpl} ({@code saveCapturedPhoto}, {@code confirmPayment}, {@code resetProcedure});
 * có thể được {@code ProcedureServiceImpl} bọc thêm trong {@link ServiceResult}.
 *
 * Ai tiêu thụ:
 * {@code ProcedureServlet} — nhánh payment / photo / reset.
 *
 * Trang / JSP:
 *
 * Attribute trên {@code candidatecall.jsp} + {@code procedure.jsp} ({@code deskMode=true}):
 * {@code profile}, {@code photoRequiredMsg}, {@code paymentErrorMsg}, …
 */
public class ProcedureActionOutcome {

    /** Kết quả lưu ảnh thủ tục tại bàn. */
    public enum PhotoStatus {
        /** Lưu ảnh thành công, path hợp lệ. */
        SUCCESS,
        /** Không tìm thấy thí sinh / enrollment theo SBD. */
        CANDIDATE_NOT_FOUND,
        /** File/base64 ảnh không hợp lệ hoặc bị từ chối. */
        INVALID_IMAGE,
        /** Lỗi hệ thống / IO khi lưu. */
        ERROR
    }

    /** Kết quả thu lệ phí / xác nhận thanh toán thủ tục. */
    public enum PaymentStatus {
        /** Thu lệ phí thành công. */
        SUCCESS,
        /** Enrollment đã thanh toán trước đó. */
        ALREADY_PAID,
        /** Chưa có ảnh hợp lệ — chặn thanh toán. */
        NO_PHOTO,
        /** Ghi nhận thanh toán thất bại. */
        PAYMENT_FAILED,
        /** Không load được hồ sơ thí sinh để thu phí. */
        PROFILE_NOT_FOUND
    }

    private PhotoStatus photoStatus = PhotoStatus.ERROR;
    private PaymentStatus paymentStatus = PaymentStatus.PAYMENT_FAILED;
    private String message;
    private String photoPath;
    private ExamRegistrationDTO profile;
    private List<ExamRegistrationDTO> queue;
    private int boardExamId;
    private String paymentAuditDetail;
    private boolean auditAllocate;
    private boolean success;
    private String sbd;
    private int candidateId;

    /** Trạng thái bước lưu ảnh trong outcome (mặc định {@link PhotoStatus#ERROR}). */
    public PhotoStatus getPhotoStatus() {
        return photoStatus;
    }

    /** Gán trạng thái lưu ảnh. */
    public void setPhotoStatus(PhotoStatus photoStatus) {
        this.photoStatus = photoStatus;
    }

    /** Trạng thái bước thanh toán lệ phí (mặc định {@link PaymentStatus#PAYMENT_FAILED}). */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /** Gán trạng thái thanh toán. */
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /** Thông điệp nghiệp vụ / lỗi để servlet bind lên UI. */
    public String getMessage() {
        return message;
    }

    /** Gán thông điệp outcome. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Đường dẫn file ảnh vừa lưu (khi photo thành công). */
    public String getPhotoPath() {
        return photoPath;
    }

    /** Gán path ảnh đã lưu. */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    /** Hồ sơ thí sinh sau cập nhật (ảnh / phí / reset) để bind lại desk. */
    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    /** Gán hồ sơ cập nhật. */
    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    /** Hàng chờ làm mới sau thao tác (nếu workflow trả về). */
    public List<ExamRegistrationDTO> getQueue() {
        return queue;
    }

    /** Gán hàng chờ kèm outcome. */
    public void setQueue(List<ExamRegistrationDTO> queue) {
        this.queue = queue;
    }

    /** ExamId bảng gọi cần sync / occupy desk sau thủ tục. */
    public int getBoardExamId() {
        return boardExamId;
    }

    /** Gán examId liên quan board. */
    public void setBoardExamId(int boardExamId) {
        this.boardExamId = boardExamId;
    }

    /** Chi tiết ghi audit khi thanh toán (chuỗi mô tả nghiệp vụ). */
    public String getPaymentAuditDetail() {
        return paymentAuditDetail;
    }

    /** Gán chi tiết audit thanh toán. */
    public void setPaymentAuditDetail(String paymentAuditDetail) {
        this.paymentAuditDetail = paymentAuditDetail;
    }

    /** true nếu sau thủ tục cần audit / kích hoạt bước phân bổ liên quan. */
    public boolean isAuditAllocate() {
        return auditAllocate;
    }

    /** Gán cờ audit allocate. */
    public void setAuditAllocate(boolean auditAllocate) {
        this.auditAllocate = auditAllocate;
    }

    /** Tổng hợp nhanh: thao tác chính vừa thực hiện có thành công không. */
    public boolean isSuccess() {
        return success;
    }

    /** Gán cờ thành công tổng quát. */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** SBD thí sinh của thao tác thủ tục. */
    public String getSbd() {
        return sbd;
    }

    /** Gán SBD thao tác. */
    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    /** Mã thí sinh (candidate id) liên quan outcome / audit. */
    public int getCandidateId() {
        return candidateId;
    }

    /** Gán mã thí sinh. */
    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
}
