package examstaff.dto;

import java.sql.Timestamp;

/**
 * DTO nhật ký audit cho trang staff (BLL → JSP {@code audit.jsp}).
 *
 * <h2>Vai trò</h2>
 * Giữ giá trị thô từ DB (table/action/old/new/details) và nhãn tiếng Việt đã chuẩn hóa
 * ({@code entityLabelVi}, {@code actionLabelVi}, {@code displayDetails}) cho hiển thị.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code AuditLogDAOImpl} → {@link StaffAuditPageViewDTO} / {@code StaffAuditPageServiceImpl}
 * → {@code AuditServlet}.
 */
public class AuditDTO {

    private String tableName;
    private String action;
    private String oldValue;
    private String newValue;
    private String details;
    private String reason;
    private Timestamp changedAt;
    /** Nhãn nghiệp vụ tiếng Việt cho hiển thị UI (không ghi DB). */
    private String entityLabelVi;
    /** Nhãn loại thao tác tiếng Việt cho hiển thị UI. */
    private String actionLabelVi;
    /** Chi tiết đã chuẩn hóa cho hiển thị UI. */
    private String displayDetails;

    /** Bản ghi rỗng — map từ ResultSet. */
    public AuditDTO() {
    }

    /** Tên bảng / entity gốc bị thay đổi. */
    public String getTableName() {
        return tableName;
    }

    /** Gán tên bảng entity. */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /** Mã action thô từ audit log (INSERT/UPDATE/… hoặc mã nghiệp vụ). */
    public String getAction() {
        return action;
    }

    /** Gán mã action. */
    public void setAction(String action) {
        this.action = action;
    }

    /** Giá trị cũ (serialized) trước thay đổi. */
    public String getOldValue() {
        return oldValue;
    }

    /** Gán giá trị cũ. */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /** Giá trị mới sau thay đổi. */
    public String getNewValue() {
        return newValue;
    }

    /** Gán giá trị mới. */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /** Chi tiết thô từ DB. */
    public String getDetails() {
        return details;
    }

    /** Gán chi tiết thô. */
    public void setDetails(String details) {
        this.details = details;
    }

    /** Lý do thay đổi (nếu có). */
    public String getReason() {
        return reason;
    }

    /** Gán lý do thay đổi. */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** Thời điểm ghi nhận thay đổi. */
    public Timestamp getChangedAt() {
        return changedAt;
    }

    /** Gán timestamp thay đổi. */
    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    /** Nhãn entity tiếng Việt cho cột UI. */
    public String getEntityLabelVi() {
        return entityLabelVi;
    }

    /** Gán nhãn entity tiếng Việt. */
    public void setEntityLabelVi(String entityLabelVi) {
        this.entityLabelVi = entityLabelVi;
    }

    /** Nhãn action tiếng Việt cho cột UI. */
    public String getActionLabelVi() {
        return actionLabelVi;
    }

    /** Gán nhãn action tiếng Việt. */
    public void setActionLabelVi(String actionLabelVi) {
        this.actionLabelVi = actionLabelVi;
    }

    /** Chi tiết đã làm sạch / format cho người xem. */
    public String getDisplayDetails() {
        return displayDetails;
    }

    /** Gán chi tiết hiển thị. */
    public void setDisplayDetails(String displayDetails) {
        this.displayDetails = displayDetails;
    }
}
