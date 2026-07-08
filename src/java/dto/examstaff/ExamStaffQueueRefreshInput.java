package dto.examstaff;

import dto.SessionDTO;

import java.util.List;

public class ExamStaffQueueRefreshInput {

    private int examId;
    private int sessionId;
    private String webRoot;
    private List<SessionDTO> allSessions;
    private Integer selectedSessionId;
    private List<String> callQueueOrder;
    private Integer callQueueOrderSessionId;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public List<SessionDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<SessionDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public Integer getSelectedSessionId() {
        return selectedSessionId;
    }

    public void setSelectedSessionId(Integer selectedSessionId) {
        this.selectedSessionId = selectedSessionId;
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
}
