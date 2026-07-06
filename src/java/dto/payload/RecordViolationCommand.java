package dto.payload;

public class RecordViolationCommand {

    private int sessionId;
    private int sbd;
    private String reasonCode;
    private String reasonDetail;
    private String evidencePath;
    private int[] deductionIds;
    private Integer actionUserId;
    private boolean isTheory;
    private String sectionName;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSbd() {
        return sbd;
    }

    public void setSbd(int sbd) {
        this.sbd = sbd;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDetail() {
        return reasonDetail;
    }

    public void setReasonDetail(String reasonDetail) {
        this.reasonDetail = reasonDetail;
    }

    public String getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(String evidencePath) {
        this.evidencePath = evidencePath;
    }

    public int[] getDeductionIds() {
        return deductionIds;
    }

    public void setDeductionIds(int[] deductionIds) {
        this.deductionIds = deductionIds;
    }

    public Integer getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(Integer actionUserId) {
        this.actionUserId = actionUserId;
    }

    public boolean isTheory() {
        return isTheory;
    }

    public void setTheory(boolean theory) {
        isTheory = theory;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
