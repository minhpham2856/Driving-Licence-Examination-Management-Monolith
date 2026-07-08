package dto.examstaff;

public class CandidateCallBoardStateDTO {

    private String callingSbd;
    private String nextSbd;
    private boolean shiftEnded;
    private long updatedAtMs;

    public String getCallingSbd() {
        return callingSbd;
    }

    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    public String getNextSbd() {
        return nextSbd;
    }

    public void setNextSbd(String nextSbd) {
        this.nextSbd = nextSbd;
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
}
