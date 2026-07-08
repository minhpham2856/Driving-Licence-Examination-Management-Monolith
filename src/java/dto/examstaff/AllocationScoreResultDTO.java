package dto.examstaff;

public class AllocationScoreResultDTO {

    private boolean saved;
    private String passedFlag;
    private String auditDetail;
    private String errorMessage;

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getPassedFlag() {
        return passedFlag;
    }

    public void setPassedFlag(String passedFlag) {
        this.passedFlag = passedFlag;
    }

    public String getAuditDetail() {
        return auditDetail;
    }

    public void setAuditDetail(String auditDetail) {
        this.auditDetail = auditDetail;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
