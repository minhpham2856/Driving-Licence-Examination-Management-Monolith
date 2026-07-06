package dto.payload;

public class CandidateSessionCommand {

    private int sessionId;
    private int sbd;
    private Integer actionUserId;
    private String sectionKeyword;
    private Boolean sectionPassedHint;

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

    public Integer getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(Integer actionUserId) {
        this.actionUserId = actionUserId;
    }

    public String getSectionKeyword() {
        return sectionKeyword;
    }

    public void setSectionKeyword(String sectionKeyword) {
        this.sectionKeyword = sectionKeyword;
    }

    public Boolean getSectionPassedHint() {
        return sectionPassedHint;
    }

    public void setSectionPassedHint(Boolean sectionPassedHint) {
        this.sectionPassedHint = sectionPassedHint;
    }
}
