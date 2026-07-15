package examstaff.dto;

/**
 * Kết quả thao tác phân công sát hạch viên (assign / unassign / …) từ BLL về Presentation.
 *
 * <h2>Vai trò</h2>
 * Mang success, alert/error và cặp action/details để servlet ghi audit nếu cần.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ExaminerAllocationDeskServiceImpl} (và service liên quan) → bọc {@link ServiceResult} →
 * {@code ExaminerAllocationServlet} → flash trên {@code examiner-allocation.jsp}.
 */
public class ExaminerAllocationActionResultDTO {

    private boolean success;
    private String alertMsg;
    private String errorMsg;
    private String auditAction;
    private String auditDetails;

    /** true nếu thao tác phân công thành công. */
    public boolean isSuccess() {
        return success;
    }

    /** Gán kết quả thành công / thất bại. */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** Thông báo thành công / cảnh báo hiển thị UI. */
    public String getAlertMsg() {
        return alertMsg;
    }

    /** Gán thông báo alert. */
    public void setAlertMsg(String alertMsg) {
        this.alertMsg = alertMsg;
    }

    /** Thông báo lỗi khi thất bại. */
    public String getErrorMsg() {
        return errorMsg;
    }

    /** Gán thông báo lỗi. */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /** Mã / tên action ghi nhật ký audit. */
    public String getAuditAction() {
        return auditAction;
    }

    /** Gán action audit. */
    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    /** Chi tiết mô tả thay đổi để ghi audit. */
    public String getAuditDetails() {
        return auditDetails;
    }

    /** Gán chi tiết audit. */
    public void setAuditDetails(String auditDetails) {
        this.auditDetails = auditDetails;
    }
}
