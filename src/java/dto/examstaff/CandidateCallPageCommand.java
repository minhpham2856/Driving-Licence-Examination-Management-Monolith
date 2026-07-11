package dto.examstaff;

import dto.exam.ExamRegistrationDTO;
import model.view.CallBoardState;

import java.util.ArrayList;
import java.util.List;

/** Input cho orchestrator trang gọi thí sinh (không phụ thuộc Servlet API). */
public class CandidateCallPageCommand {

    private String action;
    private String sbd;
    private String view;
    private String returnView;
    private int examId;
    private int boardSessionId;
    private int calledByStaffId;
    private String webRoot;
    private boolean shiftEnded;
    private boolean shiftPaused;
    private String callingSbd;
    private Integer lastLoadedExamId;
    private List<String> callQueueOrder;
    private Integer callQueueOrderSessionId;
    private List<ExamRegistrationDTO> permanentAbsents = new ArrayList<>();
    private List<ExamRegistrationDTO> cachedQueue = new ArrayList<>();
    private CallBoardState board;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getReturnView() {
        return returnView;
    }

    public void setReturnView(String returnView) {
        this.returnView = returnView;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getBoardSessionId() {
        return boardSessionId;
    }

    public void setBoardSessionId(int boardSessionId) {
        this.boardSessionId = boardSessionId;
    }

    public int getCalledByStaffId() {
        return calledByStaffId;
    }

    public void setCalledByStaffId(int calledByStaffId) {
        this.calledByStaffId = calledByStaffId;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
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

    public String getCallingSbd() {
        return callingSbd;
    }

    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    public Integer getLastLoadedExamId() {
        return lastLoadedExamId;
    }

    public void setLastLoadedExamId(Integer lastLoadedExamId) {
        this.lastLoadedExamId = lastLoadedExamId;
    }

    public List<String> getCallQueueOrder() {
        return callQueueOrder;
    }

    public void setCallQueueOrder(List<String> callQueueOrder) {
        this.callQueueOrder = callQueueOrder;
    }

    public Integer getCallQueueOrderSessionId() {
        return callQueueOrderSessionId;
    }

    public void setCallQueueOrderSessionId(Integer callQueueOrderSessionId) {
        this.callQueueOrderSessionId = callQueueOrderSessionId;
    }

    public List<ExamRegistrationDTO> getPermanentAbsents() {
        return permanentAbsents;
    }

    public void setPermanentAbsents(List<ExamRegistrationDTO> permanentAbsents) {
        this.permanentAbsents = permanentAbsents != null ? permanentAbsents : new ArrayList<>();
    }

    public List<ExamRegistrationDTO> getCachedQueue() {
        return cachedQueue;
    }

    public void setCachedQueue(List<ExamRegistrationDTO> cachedQueue) {
        this.cachedQueue = cachedQueue != null ? cachedQueue : new ArrayList<>();
    }

    public CallBoardState getBoard() {
        return board;
    }

    public void setBoard(CallBoardState board) {
        this.board = board;
    }
}
