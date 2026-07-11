package model.view;

import java.util.ArrayList;
import java.util.List;

/** Trạng thái bảng gọi thí sinh (không phụ thuộc Servlet API). */
public class CallBoardState {

    private int examSessionId;
    private String callingSbd;
    private String nextSbd;
    private boolean shiftEnded;
    private boolean examPaused;
    private long updatedAtMs;
    private List<String> queueOrderSbds = new ArrayList<>();
    private boolean deskBusy;
    private String deskSbd;

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

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

    public List<String> getQueueOrderSbds() {
        return queueOrderSbds;
    }

    public void setQueueOrderSbds(List<String> queueOrderSbds) {
        this.queueOrderSbds = queueOrderSbds != null ? new ArrayList<>(queueOrderSbds) : new ArrayList<>();
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
