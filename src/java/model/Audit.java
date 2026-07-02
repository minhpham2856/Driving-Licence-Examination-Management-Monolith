package model;
import java.sql.Timestamp;
public class Audit {
    private long auditId;
    private Integer userId;
    private String action;
    private String reason;
    private String entityName;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String details;
    private Timestamp createdAt;
    public Audit() {
    }
    public Audit(long auditId, Integer userId, String action, String reason, String entityName, String entityId, String oldValue, String newValue, String details, Timestamp createdAt) {
        this.auditId = auditId;
        this.userId = userId;
        this.action = action;
        this.reason = reason;
        this.entityName = entityName;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
        this.createdAt = createdAt;
    }
    public long getAuditId() {
        return auditId;
    }
    public void setAuditId(long auditId) {
        this.auditId = auditId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getEntityName() {
        return entityName;
    }
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    public String getEntityId() {
        return entityId;
    }
    public void setEntityId(String entityId) {
        this.entityId = entityId;
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
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
