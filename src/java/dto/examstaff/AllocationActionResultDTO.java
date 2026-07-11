package dto.examstaff;

public class AllocationActionResultDTO {

    private String errorMsg;
    private String alertMsg;
    private String auditAction;
    private String auditDetails;
    private int auditRecordId;
    private int allocatedCount;
    private String redirectServletPath;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getAlertMsg() {
        return alertMsg;
    }

    public void setAlertMsg(String alertMsg) {
        this.alertMsg = alertMsg;
    }

    public String getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    public String getAuditDetails() {
        return auditDetails;
    }

    public void setAuditDetails(String auditDetails) {
        this.auditDetails = auditDetails;
    }

    public int getAuditRecordId() {
        return auditRecordId;
    }

    public void setAuditRecordId(int auditRecordId) {
        this.auditRecordId = auditRecordId;
    }

    public int getAllocatedCount() {
        return allocatedCount;
    }

    public void setAllocatedCount(int allocatedCount) {
        this.allocatedCount = allocatedCount;
    }

    public String getRedirectServletPath() {
        return redirectServletPath;
    }

    public void setRedirectServletPath(String redirectServletPath) {
        this.redirectServletPath = redirectServletPath;
    }

    public boolean hasAuditLog() {
        return auditAction != null && auditDetails != null;
    }
}
