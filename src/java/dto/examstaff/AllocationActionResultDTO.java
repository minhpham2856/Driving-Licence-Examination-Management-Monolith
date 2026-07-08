package dto.examstaff;

public class AllocationActionResultDTO {

    private String errorMsg;
    private String warningMsg;
    private String alertMsg;
    private String auditAction;
    private String auditDetails;
    private int auditRecordId;
    private int allocatedCount;
    private String redirectServletPath;
    private boolean syncCallBoard;
    private String callingSbd;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
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

    public boolean isSyncCallBoard() {
        return syncCallBoard;
    }

    public void setSyncCallBoard(boolean syncCallBoard) {
        this.syncCallBoard = syncCallBoard;
    }

    public String getCallingSbd() {
        return callingSbd;
    }

    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    public boolean hasAuditLog() {
        return auditAction != null && auditDetails != null;
    }
}
