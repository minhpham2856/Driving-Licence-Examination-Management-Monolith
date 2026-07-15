package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Kết quả một hành động gọi thí sinh từ BLL workflow về Presentation / page orchestrator.
 *
 * <h2>Vai trò</h2>
 * Cập nhật hàng chờ, SBD đang gọi, cờ ca và loại cảnh báo UI sau call/absent/undo…
 * Thường được merge vào {@link CandidateCallPageViewDTO} bởi {@code CandidateCallPageServiceImpl}.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code CandidateCallWorkflowServiceImpl} → page service → alert trên {@code candidatecall.jsp}.
 */
public class CandidateCallActionResultDTO {

    /** Loại cảnh báo UI sau thao tác gọi thí sinh. */
    public enum AlertType {
        /** Không có cảnh báo. */
        NONE,
        /** Tự động đánh vắng khi quá thời gian / không phản hồi. */
        AUTO_ABSENT,
        /** Đánh vắng (có thể có mặt lại sau). */
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

    /** Full queue sau action. */
    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    /** Gán full queue sau action. */
    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue;
    }

    /** Active queue sau action. */
    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    /** Gán active queue sau action. */
    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue;
    }

    /** SBD đang gọi sau action (nếu có). */
    public String getCallingSbd() {
        return callingSbd;
    }

    /** Gán SBD đang gọi. */
    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    /** Yêu cầu xóa callingSbd trên session. */
    public boolean isClearCallingSbd() {
        return clearCallingSbd;
    }

    /** Gán cờ clear calling. */
    public void setClearCallingSbd(boolean clearCallingSbd) {
        this.clearCallingSbd = clearCallingSbd;
    }

    /** Yêu cầu reload queue từ DB sau action. */
    public boolean isReloadQueue() {
        return reloadQueue;
    }

    /** Gán cờ reload queue. */
    public void setReloadQueue(boolean reloadQueue) {
        this.reloadQueue = reloadQueue;
    }

    /** Ca đã kết thúc sau action. */
    public boolean isShiftEnded() {
        return shiftEnded;
    }

    /** Gán cờ ca ended. */
    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    /** Ca đang pause sau action. */
    public boolean isShiftPaused() {
        return shiftPaused;
    }

    /** Gán cờ ca paused. */
    public void setShiftPaused(boolean shiftPaused) {
        this.shiftPaused = shiftPaused;
    }

    /** Yêu cầu redirect về trang gọi chính. */
    public boolean isRedirectToCallPage() {
        return redirectToCallPage;
    }

    /** Gán cờ redirect về call page. */
    public void setRedirectToCallPage(boolean redirectToCallPage) {
        this.redirectToCallPage = redirectToCallPage;
    }

    /** Đồng bộ lại thứ tự queue session với kết quả mới. */
    public boolean isSyncQueueOrder() {
        return syncQueueOrder;
    }

    /** Gán cờ sync thứ tự queue. */
    public void setSyncQueueOrder(boolean syncQueueOrder) {
        this.syncQueueOrder = syncQueueOrder;
    }

    /** Đẩy thí sinh vừa khôi phục lên đầu hàng. */
    public boolean isMoveRestoredToFront() {
        return moveRestoredToFront;
    }

    /** Gán cờ đẩy thí sinh khôi phục lên trước. */
    public void setMoveRestoredToFront(boolean moveRestoredToFront) {
        this.moveRestoredToFront = moveRestoredToFront;
    }

    /** SBD chèn ngay sau đó khi promote thí sinh khôi phục. */
    public String getPromoteAfterSbd() {
        return promoteAfterSbd;
    }

    /** Gán SBD neo khi promote. */
    public void setPromoteAfterSbd(String promoteAfterSbd) {
        this.promoteAfterSbd = promoteAfterSbd;
    }

    /** Loại cảnh báo UI (mặc định {@link AlertType#NONE}). */
    public AlertType getAlertType() {
        return alertType;
    }

    /** Gán loại alert. */
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    /** SBD gắn với banner alert. */
    public String getAlertSbd() {
        return alertSbd;
    }

    /** Gán SBD của alert. */
    public void setAlertSbd(String alertSbd) {
        this.alertSbd = alertSbd;
    }
}
