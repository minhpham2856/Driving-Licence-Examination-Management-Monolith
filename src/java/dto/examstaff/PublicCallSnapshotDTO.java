package dto.examstaff;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class PublicCallSnapshotDTO {

    private int sessionId;
    private SessionDTO currentSession;
    private ExamRegistrationDTO callingCandidate;
    private ExamRegistrationDTO nextCandidate;
    private List<ExamRegistrationDTO> waitingQueue;
    private boolean callingActive;
    private boolean shiftEnded;
    private long updatedAtMs;
    private boolean deskBusy;
    private String deskSbd;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public SessionDTO getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(SessionDTO currentSession) {
        this.currentSession = currentSession;
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
