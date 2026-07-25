package registrant.dto;

import java.sql.Timestamp;

/**
 * DTO ánh xạ một dòng bảng Audit — dùng ghi log và hiển thị timeline theo dõi hồ sơ.
 * Chứa EntityName, Action, giá trị cũ/mới, Details/Reason, thời điểm và tên người thực hiện (changerName join từ User/Profile).
 */
public class AuditLogEntry {
    private long id;
    private String tableName;
    private Integer recordId;
    private String action;
    private String oldValue;
    private String newValue;
    private int changedBy;
    private Timestamp changedAt;
    private String ipAddress;
    private String sessionId;

    private String reason;
    private String details;

    // Additional helper field for display
    private String changerName;

    public AuditLogEntry() {
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChangerName() {
        return changerName;
    }

    public void setChangerName(String changerName) {
        this.changerName = changerName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Helper method to get time in HH:mm format for compatibility
    public String getTime() {
        if (changedAt == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(changedAt);
    }
}
