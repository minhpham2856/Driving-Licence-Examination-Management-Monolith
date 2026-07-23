package examstaff.dto;

/**
 * Kết quả thao tác phân bổ thí sinh (BLL → Presentation).
 *
 * Vai trò:
 * Mang alert/error, thông tin audit, số lượng đã phân và đường redirect sau xử lý
 * (allocate một / hàng loạt).
 *
 * Ai tạo / tiêu thụ:
 * {@code AllocationActionServiceImpl}, {@code ExaminerAllocationServiceImpl}
 * → thường bọc {@link ServiceResult} → {@code AllocationServlet} flash + PRG.
 */
public class AllocationActionResultDTO {

    private String errorMsg;
    private String alertMsg;
    private String auditAction;
    private String auditDetails;
    private int auditRecordId;
    private int allocatedCount;
    private String redirectServletPath;

    /** Thông báo lỗi khi phân bổ thất bại. */
    public String getErrorMsg() {
        return errorMsg;
    }

    /** Gán thông báo lỗi. */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /** Thông báo thành công / cảnh báo UI. */
    public String getAlertMsg() {
        return alertMsg;
    }

    /** Gán thông báo alert. */
    public void setAlertMsg(String alertMsg) {
        this.alertMsg = alertMsg;
    }

    /** Mã action audit (ALLOCATE, UNALLOCATE…). */
    public String getAuditAction() {
        return auditAction;
    }

    /** Gán action audit. */
    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    /** Chi tiết thay đổi để ghi nhật ký. */
    public String getAuditDetails() {
        return auditDetails;
    }

    /** Gán chi tiết audit. */
    public void setAuditDetails(String auditDetails) {
        this.auditDetails = auditDetails;
    }

    /** Id bản ghi liên quan audit (registration / enrollment). */
    public int getAuditRecordId() {
        return auditRecordId;
    }

    /** Gán id bản ghi audit. */
    public void setAuditRecordId(int auditRecordId) {
        this.auditRecordId = auditRecordId;
    }

    /** Số thí sinh đã phân bổ thành công trong batch (nếu có). */
    public int getAllocatedCount() {
        return allocatedCount;
    }

    /** Gán số lượng đã phân. */
    public void setAllocatedCount(int allocatedCount) {
        this.allocatedCount = allocatedCount;
    }

    /** Đường servlet redirect sau thao tác (PRG). */
    public String getRedirectServletPath() {
        return redirectServletPath;
    }

    /** Gán path redirect. */
    public void setRedirectServletPath(String redirectServletPath) {
        this.redirectServletPath = redirectServletPath;
    }

    /**
     * Có đủ action + details để servlet ghi nhật ký audit.
     * @return true khi cả {@code auditAction} và {@code auditDetails} khác null
     */
    public boolean hasAuditLog() {
        return auditAction != null && auditDetails != null;
    }
}
