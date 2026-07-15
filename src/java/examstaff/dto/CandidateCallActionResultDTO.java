package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Kết quả một hành động gọi thí sinh từ BLL về Presentation.
 * Cập nhật hàng chờ, SBD đang gọi, cờ ca và loại cảnh báo UI.
 */
public class CandidateCallActionResultDTO {

    /** Loại cảnh báo UI sau thao tác gọi thí sinh. */
    public enum AlertType {
        /** Không có cảnh báo. */
        NONE,
        /** Tự động đánh vắng khi quá thời gian / không phản hồi. */
        AUTO_ABSENT,
        /** Đánh vắng (có mặt lại sau). */
        ABSENT,
        /** Vắng mặt vĩnh viễn trong ca. */
        PERMANENT_ABSENT,
        /** Hoàn tác thao tác trước đó. */
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
    /** SBD sau đó cần đẩy thí sinh được khôi phục lên trước. */
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
