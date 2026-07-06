package dto.payload;

import enums.ExamSection;
import model.User;

public class CallCandidateCommand {

    private int sessionId;
    private Integer sbd;
    private int[] sbds;
    private User user;
    private Integer actionUserId;
    private boolean isTheory;
    private ExamSection examSection;
    private String sectionName;
    private String callDestination;
    private boolean scoreEntry;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSbd() {
        return sbd;
    }

    public void setSbd(Integer sbd) {
        this.sbd = sbd;
    }

    public int[] getSbds() {
        return sbds;
    }

    public void setSbds(int[] sbds) {
        this.sbds = sbds;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public ExamSection getExamSection() {
        return examSection;
    }

    public void setExamSection(ExamSection examSection) {
        this.examSection = examSection;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getCallDestination() {
        return callDestination;
    }

    public void setCallDestination(String callDestination) {
        this.callDestination = callDestination;
    }

    public boolean isScoreEntry() {
        return scoreEntry;
    }

    public void setScoreEntry(boolean scoreEntry) {
        this.scoreEntry = scoreEntry;
    }
}
