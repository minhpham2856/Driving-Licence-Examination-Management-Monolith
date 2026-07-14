package examstaff.dto.user;

import java.sql.Timestamp;

/**
 * DTO nhật ký audit cho trang staff (BLL → JSP).
 * Giữ giá trị thô từ DB và nhãn tiếng Việt đã chuẩn hóa cho hiển thị.
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

    public AuditDTO() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String getEntityLabelVi() {
        return entityLabelVi;
    }

    public void setEntityLabelVi(String entityLabelVi) {
        this.entityLabelVi = entityLabelVi;
    }

    public String getActionLabelVi() {
        return actionLabelVi;
    }

    public void setActionLabelVi(String actionLabelVi) {
        this.actionLabelVi = actionLabelVi;
    }

    public String getDisplayDetails() {
        return displayDetails;
    }

    public void setDisplayDetails(String displayDetails) {
        this.displayDetails = displayDetails;
    }
}
