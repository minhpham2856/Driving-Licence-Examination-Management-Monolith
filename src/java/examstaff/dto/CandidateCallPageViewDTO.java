package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả orchestrator trang gọi thí sinh — BLL → Presentation để servlet áp dụng side-effect
 * session / board rồi forward JSP.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Sau khi {@code CandidateCallPageServiceImpl} xử lý {@link CandidateCallPageCommand}, DTO này mang:
 * các list hàng chờ (full / active / suspended), SBD đang gọi, cờ ca, và các chỉ thị side-effect
 * (release desk, sync board, persist queue order, clear SBD vừa thanh toán thủ tục, alert UI).
 *
 * <h2>Ai tạo</h2>
 * {@code CandidateCallPageServiceImpl} (qua facade {@code StaffCallServiceImpl#preparePage}).
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code CandidateCallServlet} — {@code applyCallSideEffects}, {@code applyBoardOp}, {@code bindActionAlert}.
 *
 * <h2>Trang / JSP</h2>
 * {@code candidatecall.jsp}, {@code candidate-suspended.jsp} (attributes lấy từ view + session sau bind).
 */
public class CandidateCallPageViewDTO {

    private String redirectPath;
    private boolean resumeShift;
    private List<ExamRegistrationDTO> fullQueue = new ArrayList<>();
    private List<ExamRegistrationDTO> activeQueue = new ArrayList<>();
    private List<ExamRegistrationDTO> suspendedList = new ArrayList<>();
    private boolean showSuspended;
    private ExamRegistrationDTO nextCallingCandidate;
    private String callingSbd;
    private boolean clearCallingSbd;
    private boolean shiftEnded;
    private boolean shiftPaused;
    private boolean pauseBoard;
    private boolean resumeBoard;
    private boolean clearProcedureJustPaidSbd;
    private boolean persistQueueOrder;
    private CandidateCallActionResultDTO.AlertType alertType = CandidateCallActionResultDTO.AlertType.NONE;
    private String alertSbd;
    private boolean releaseDesk;
    private String releaseDeskCallingSbd;
    private boolean syncBoard;
    private String boardCallingSbd;
    private int publishExamId;

    /** Đường dẫn redirect tương đối nếu cần PRG thay vì forward (null = ở lại trang). */
    public String getRedirectPath() {
        return redirectPath;
    }

    /** Gán đường redirect sau xử lý. */
    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    /** Yêu cầu servlet mở lại ca (bỏ ended/paused) trước khi bind view. */
    public boolean isResumeShift() {
        return resumeShift;
    }

    /** Gán cờ resume ca. */
    public void setResumeShift(boolean resumeShift) {
        this.resumeShift = resumeShift;
    }

    /** Hàng chờ đầy đủ (mọi thí sinh liên quan kỳ sau lọc nghiệp vụ). */
    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    /** Gán full queue (null → list rỗng). */
    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue != null ? fullQueue : new ArrayList<>();
    }

    /** Hàng chờ đang gọi được (active — chưa vắng/đình chỉ/xong…). */
    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    /** Gán active queue (null → list rỗng). */
    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue != null ? activeQueue : new ArrayList<>();
    }

    /** Danh sách thí sinh đang đình chỉ (màn suspended). */
    public List<ExamRegistrationDTO> getSuspendedList() {
        return suspendedList;
    }

    /** Gán list đình chỉ (null → list rỗng). */
    public void setSuspendedList(List<ExamRegistrationDTO> suspendedList) {
        this.suspendedList = suspendedList != null ? suspendedList : new ArrayList<>();
    }

    /** true nếu UI nên hiển thị / forward sang view danh sách đình chỉ. */
    public boolean isShowSuspended() {
        return showSuspended;
    }

    /** Gán cờ hiển thị màn suspended. */
    public void setShowSuspended(boolean showSuspended) {
        this.showSuspended = showSuspended;
    }

    /** Thí sinh tiếp theo nên hiển thị / gọi trên bàn. */
    public ExamRegistrationDTO getNextCallingCandidate() {
        return nextCallingCandidate;
    }

    /** Gán thí sinh kế tiếp trên bàn gọi. */
    public void setNextCallingCandidate(ExamRegistrationDTO nextCallingCandidate) {
        this.nextCallingCandidate = nextCallingCandidate;
    }

    /** SBD đang gọi sau khi xử lý (đưa vào session / board). */
    public String getCallingSbd() {
        return callingSbd;
    }

    /** Gán SBD đang gọi trên view result. */
    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    /** Yêu cầu xóa callingSbd khỏi session (kết thúc slot gọi hiện tại). */
    public boolean isClearCallingSbd() {
        return clearCallingSbd;
    }

    /** Gán cờ clear SBD đang gọi. */
    public void setClearCallingSbd(boolean clearCallingSbd) {
        this.clearCallingSbd = clearCallingSbd;
    }

    /** Trạng thái ca đã kết thúc cần phản ánh lên UI / board. */
    public boolean isShiftEnded() {
        return shiftEnded;
    }

    /** Gán cờ ca ended trên kết quả trang. */
    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    /** Trạng thái ca đang pause cần phản ánh lên UI. */
    public boolean isShiftPaused() {
        return shiftPaused;
    }

    /** Gán cờ ca paused. */
    public void setShiftPaused(boolean shiftPaused) {
        this.shiftPaused = shiftPaused;
    }

    /** Yêu cầu servlet pause bảng gọi (CallBoard) tương ứng action control. */
    public boolean isPauseBoard() {
        return pauseBoard;
    }

    /** Gán chỉ thị pause board. */
    public void setPauseBoard(boolean pauseBoard) {
        this.pauseBoard = pauseBoard;
    }

    /** Yêu cầu servlet tiếp tục CallBoard sau tạm dừng gọi số. */
    public boolean isResumeBoard() {
        return resumeBoard;
    }

    /** Gán chỉ thị resume board. */
    public void setResumeBoard(boolean resumeBoard) {
        this.resumeBoard = resumeBoard;
    }

    /** Xóa flash SBD vừa thanh toán thủ tục trên session (tránh banner cũ). */
    public boolean isClearProcedureJustPaidSbd() {
        return clearProcedureJustPaidSbd;
    }

    /** Gán cờ clear SBD vừa paid thủ tục. */
    public void setClearProcedureJustPaidSbd(boolean clearProcedureJustPaidSbd) {
        this.clearProcedureJustPaidSbd = clearProcedureJustPaidSbd;
    }

    /** Persist thứ tự hàng gọi (SBD) vào session sau thao tác. */
    public boolean isPersistQueueOrder() {
        return persistQueueOrder;
    }

    /** Gán cờ lưu lại thứ tự queue. */
    public void setPersistQueueOrder(boolean persistQueueOrder) {
        this.persistQueueOrder = persistQueueOrder;
    }

    /** Loại cảnh báo UI (auto-absent, absent, undo, …). */
    public CandidateCallActionResultDTO.AlertType getAlertType() {
        return alertType;
    }

    /** Gán loại alert (null → {@code NONE}). */
    public void setAlertType(CandidateCallActionResultDTO.AlertType alertType) {
        this.alertType = alertType != null ? alertType : CandidateCallActionResultDTO.AlertType.NONE;
    }

    /** SBD gắn với thông báo alert (highlight trên UI). */
    public String getAlertSbd() {
        return alertSbd;
    }

    /** Gán SBD của alert. */
    public void setAlertSbd(String alertSbd) {
        this.alertSbd = alertSbd;
    }

    /** Yêu cầu nhả bàn thủ tục (deskBusy = false) sau action. */
    public boolean isReleaseDesk() {
        return releaseDesk;
    }

    /** Gán chỉ thị release desk. */
    public void setReleaseDesk(boolean releaseDesk) {
        this.releaseDesk = releaseDesk;
    }

    /** SBD đang giữ desk cần đối chiếu khi release (tránh nhả nhầm thí sinh khác). */
    public String getReleaseDeskCallingSbd() {
        return releaseDeskCallingSbd;
    }

    /** Gán SBD đối chiếu khi release desk. */
    public void setReleaseDeskCallingSbd(String releaseDeskCallingSbd) {
        this.releaseDeskCallingSbd = releaseDeskCallingSbd;
    }

    /** Yêu cầu đồng bộ CallBoardState (calling/next/queue) với trạng thái mới. */
    public boolean isSyncBoard() {
        return syncBoard;
    }

    /** Gán chỉ thị sync board. */
    public void setSyncBoard(boolean syncBoard) {
        this.syncBoard = syncBoard;
    }

    /** SBD calling sẽ publish lên board khi sync. */
    public String getBoardCallingSbd() {
        return boardCallingSbd;
    }

    /** Gán SBD publish lên bảng gọi. */
    public void setBoardCallingSbd(String boardCallingSbd) {
        this.boardCallingSbd = boardCallingSbd;
    }

    /** ExamId dùng khi publish / sync board (Public Call đọc exam này). */
    public int getPublishExamId() {
        return publishExamId;
    }

    /** Gán examId publish bảng gọi. */
    public void setPublishExamId(int publishExamId) {
        this.publishExamId = publishExamId;
    }

}
