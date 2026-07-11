package dto.examstaff;

import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class CandidateCallActionResultDTO {

    public enum AlertType {
        NONE,
        AUTO_ABSENT,
        ABSENT,
        PERMANENT_ABSENT,
        UNDO
    }

    private List<ExamRegistrationDTO> fullQueue;
    private List<ExamRegistrationDTO> activeQueue;
    private String callingSbd;
    private boolean clearCallingSbd;
    private boolean reloadQueue;
    private boolean shiftEnded;
    private boolean shiftPaused;
    private boolean redirectToCallPage;
    private boolean syncQueueOrder;
    private boolean moveRestoredToFront;
    private String promoteAfterSbd;
    private AlertType alertType = AlertType.NONE;
    private String alertSbd;

    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue;
    }

    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue;
    }

    public String getCallingSbd() {
        return callingSbd;
    }

    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    public boolean isClearCallingSbd() {
        return clearCallingSbd;
    }

    public void setClearCallingSbd(boolean clearCallingSbd) {
        this.clearCallingSbd = clearCallingSbd;
    }

    public boolean isReloadQueue() {
        return reloadQueue;
    }

    public void setReloadQueue(boolean reloadQueue) {
        this.reloadQueue = reloadQueue;
    }

    public boolean isShiftEnded() {
        return shiftEnded;
    }

    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    public boolean isShiftPaused() {
        return shiftPaused;
    }

    public void setShiftPaused(boolean shiftPaused) {
        this.shiftPaused = shiftPaused;
    }

    public boolean isRedirectToCallPage() {
        return redirectToCallPage;
    }

    public void setRedirectToCallPage(boolean redirectToCallPage) {
        this.redirectToCallPage = redirectToCallPage;
    }

    public boolean isSyncQueueOrder() {
        return syncQueueOrder;
    }

    public void setSyncQueueOrder(boolean syncQueueOrder) {
        this.syncQueueOrder = syncQueueOrder;
    }

    public boolean isMoveRestoredToFront() {
        return moveRestoredToFront;
    }

    public void setMoveRestoredToFront(boolean moveRestoredToFront) {
        this.moveRestoredToFront = moveRestoredToFront;
    }

    public String getPromoteAfterSbd() {
        return promoteAfterSbd;
    }

    public void setPromoteAfterSbd(String promoteAfterSbd) {
        this.promoteAfterSbd = promoteAfterSbd;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getAlertSbd() {
        return alertSbd;
    }

    public void setAlertSbd(String alertSbd) {
        this.alertSbd = alertSbd;
    }
}
