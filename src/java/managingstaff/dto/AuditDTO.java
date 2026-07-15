package managingstaff.dto;

import java.sql.Timestamp;

public class AuditDTO {
    private long id;
    private String action;
    private String tableName;
    private String recordId;
    private String oldValue;
    private String newValue;
    private String reason;
    private String details;
    private Timestamp changedAt;
    private String changerName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }
    public String getChangerName() { return changerName; }
    public void setChangerName(String changerName) { this.changerName = changerName; }
    public String getIpAddress() { return null; }
    public String getSessionId() { return null; }
}
