package examstaff.dto.user;


import java.sql.Timestamp;

public class AuditDTO {

    private long id;
    private String tableName;
    private Integer recordId;
    private String action;
    private String oldValue;
    private String newValue;
    private String details;
    private String reason;
    private int changedBy;
    private Timestamp changedAt;
    private String changerName;
    private String ipAddress;
    private String sessionId;
    /** Nhãn nghiệp vụ tiếng Việt cho hiển thị UI (không ghi DB). */
    private String entityLabelVi;
    /** Nhãn loại thao tác tiếng Việt cho hiển thị UI. */
    private String actionLabelVi;
    /** Chi tiết đã chuẩn hóa cho hiển thị UI. */
    private String displayDetails;

    public AuditDTO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
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

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangerName() {
        return changerName;
    }

    public void setChangerName(String changerName) {
        this.changerName = changerName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
