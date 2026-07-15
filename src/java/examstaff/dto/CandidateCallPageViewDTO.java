package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả orchestrator trang gọi thí sinh để servlet bind session/view.
 * Mang hàng chờ, cờ ca/board/desk và alert sau khi xử lý.
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
    private boolean clearProcedureJustPaidSbd;
    private boolean persistQueueOrder;
    private CandidateCallActionResultDTO.AlertType alertType = CandidateCallActionResultDTO.AlertType.NONE;
    private String alertSbd;
    private boolean releaseDesk;
    private String releaseDeskCallingSbd;
    private boolean syncBoard;
    private String boardCallingSbd;
    private int publishExamId;

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public boolean isResumeShift() {
        return resumeShift;
    }

    public void setResumeShift(boolean resumeShift) {
        this.resumeShift = resumeShift;
    }

    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue != null ? fullQueue : new ArrayList<>();
    }

    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue != null ? activeQueue : new ArrayList<>();
    }

    public List<ExamRegistrationDTO> getSuspendedList() {
        return suspendedList;
    }

    public void setSuspendedList(List<ExamRegistrationDTO> suspendedList) {
        this.suspendedList = suspendedList != null ? suspendedList : new ArrayList<>();
    }

    public boolean isShowSuspended() {
        return showSuspended;
    }

    public void setShowSuspended(boolean showSuspended) {
        this.showSuspended = showSuspended;
    }

    public ExamRegistrationDTO getNextCallingCandidate() {
        return nextCallingCandidate;
    }

    public void setNextCallingCandidate(ExamRegistrationDTO nextCallingCandidate) {
        this.nextCallingCandidate = nextCallingCandidate;
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

    public boolean isPauseBoard() {
        return pauseBoard;
    }

    public void setPauseBoard(boolean pauseBoard) {
        this.pauseBoard = pauseBoard;
    }

    public boolean isClearProcedureJustPaidSbd() {
        return clearProcedureJustPaidSbd;
    }

    public void setClearProcedureJustPaidSbd(boolean clearProcedureJustPaidSbd) {
        this.clearProcedureJustPaidSbd = clearProcedureJustPaidSbd;
    }

    public boolean isPersistQueueOrder() {
        return persistQueueOrder;
    }

    public void setPersistQueueOrder(boolean persistQueueOrder) {
        this.persistQueueOrder = persistQueueOrder;
    }

    public CandidateCallActionResultDTO.AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(CandidateCallActionResultDTO.AlertType alertType) {
        this.alertType = alertType != null ? alertType : CandidateCallActionResultDTO.AlertType.NONE;
    }

    public String getAlertSbd() {
        return alertSbd;
    }

    public void setAlertSbd(String alertSbd) {
        this.alertSbd = alertSbd;
    }

    public boolean isReleaseDesk() {
        return releaseDesk;
    }

    public void setReleaseDesk(boolean releaseDesk) {
        this.releaseDesk = releaseDesk;
    }

    public String getReleaseDeskCallingSbd() {
        return releaseDeskCallingSbd;
    }

    public void setReleaseDeskCallingSbd(String releaseDeskCallingSbd) {
        this.releaseDeskCallingSbd = releaseDeskCallingSbd;
    }

    public boolean isSyncBoard() {
        return syncBoard;
    }

    public void setSyncBoard(boolean syncBoard) {
        this.syncBoard = syncBoard;
    }

    public String getBoardCallingSbd() {
        return boardCallingSbd;
    }

    public void setBoardCallingSbd(String boardCallingSbd) {
        this.boardCallingSbd = boardCallingSbd;
    }

    public int getPublishExamId() {
        return publishExamId;
    }

    public void setPublishExamId(int publishExamId) {
        this.publishExamId = publishExamId;
    }

}
