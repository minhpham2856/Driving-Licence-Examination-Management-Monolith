package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Snapshot chỉ-đọc cho Public Call (JSP + JSON API).
 * Gồm kỳ thi, thí sinh đang gọi / kế tiếp, hàng chờ và cờ desk/pause/end.
 */
public class PublicCallSnapshotDTO {

    private int examId;
    private ExamSummaryDTO currentExam;
    private ExamRegistrationDTO callingCandidate;
    private ExamRegistrationDTO nextCandidate;
    private List<ExamRegistrationDTO> waitingQueue;
    private boolean callingActive;
    private boolean shiftEnded;
    private boolean examPaused;
    private long updatedAtMs;
    private boolean deskBusy;
    private String deskSbd;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public ExamSummaryDTO getCurrentExam() {
        return currentExam;
    }

    public void setCurrentExam(ExamSummaryDTO currentExam) {
        this.currentExam = currentExam;
    }

    public ExamRegistrationDTO getCallingCandidate() {
        return callingCandidate;
    }

    public void setCallingCandidate(ExamRegistrationDTO callingCandidate) {
        this.callingCandidate = callingCandidate;
    }

    public ExamRegistrationDTO getNextCandidate() {
        return nextCandidate;
    }

    public void setNextCandidate(ExamRegistrationDTO nextCandidate) {
        this.nextCandidate = nextCandidate;
    }

    public List<ExamRegistrationDTO> getWaitingQueue() {
        return waitingQueue;
    }

    public void setWaitingQueue(List<ExamRegistrationDTO> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    public boolean isCallingActive() {
        return callingActive;
    }

    public void setCallingActive(boolean callingActive) {
        this.callingActive = callingActive;
    }

    public boolean isShiftEnded() {
        return shiftEnded;
    }

    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    public boolean isExamPaused() {
        return examPaused;
    }

    public void setExamPaused(boolean examPaused) {
        this.examPaused = examPaused;
    }

    public long getUpdatedAtMs() {
        return updatedAtMs;
    }

    public void setUpdatedAtMs(long updatedAtMs) {
        this.updatedAtMs = updatedAtMs;
    }

    public boolean isDeskBusy() {
        return deskBusy;
    }

    public void setDeskBusy(boolean deskBusy) {
        this.deskBusy = deskBusy;
    }

    public String getDeskSbd() {
        return deskSbd;
    }

    public void setDeskSbd(String deskSbd) {
        this.deskSbd = deskSbd;
    }
}
