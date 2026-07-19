package admin.model;

import java.sql.Timestamp;

/** Dòng nhật ký cho màn hình Nhật ký hệ thống (Audit + join User/Profile). */
public class AuditView {
    private long auditId;
    private Timestamp createdAt;
    private String fullName;
    private String username;
    private String roleDb;
    private String action;
    private String entityName;
    private String detail;

    public long getAuditId() { return auditId; }
    public void setAuditId(long v) { this.auditId = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getRoleDb() { return roleDb; }
    public void setRoleDb(String v) { this.roleDb = v; }
    public String getRoleCode() { return admin.util.RoleUi.toUiCode(roleDb); }
    public String getAction() { return action; }
    public void setAction(String v) { this.action = v; }

    public String getActionLabel() {
        if (action == null) return "-";
        switch (action.toUpperCase()) {
            case "INSERT":  return "Thêm mới";
            case "UPDATE":  return "Cập nhật";
            case "DELETE":  return "Xóa";
            case "WARNING": return "Cảnh báo";
            case "IMPORT":  return "Nhập dữ liệu";
            case "EXPORT":  return "Xuất dữ liệu";
            case "ASSIGN":  return "Phân công";
            default:        return action;
        }
    }
    public String getModule() { return (entityName == null || entityName.isBlank()) ? "-" : entityName; }
    public void setEntityName(String v) { this.entityName = v; }
    public String getDetail() { return (detail == null || detail.isBlank()) ? "-" : detail; }
    public void setDetail(String v) { this.detail = v; }
    public String getIp() { return "-"; }
    public String getInitial() {
        String s = (fullName != null && !fullName.isBlank()) ? fullName
                 : (username != null && !username.isBlank() ? username : "?");
        return s.substring(0, 1).toUpperCase();
    }
    public String getDisplayName() {
        return (fullName != null && !fullName.isBlank()) ? fullName
             : (username != null && !username.isBlank() ? username : "(không rõ)");
    }
}
