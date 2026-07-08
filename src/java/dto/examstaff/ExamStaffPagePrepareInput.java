package dto.examstaff;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ExamStaffPagePrepareInput {

    private int urlSessionId;
    private Integer previousExamId;
    private Integer previousSessionId;
    private Integer selectedExamId;
    private Integer selectedSessionId;
    private Integer loadedExamId;
    private Integer loadedSessionId;
    private String examIdParam;
    private boolean hasSessionIdParam;
    private boolean loadCandidates;
    private String webRoot;
    private List<SessionDTO> allSessions;
    private List<ExamRegistrationDTO> cachedQueue;
    private List<String> callQueueOrder;
    private Integer callQueueOrderSessionId;

    public int getUrlSessionId() {
        return urlSessionId;
    }

    public void setUrlSessionId(int urlSessionId) {
        this.urlSessionId = urlSessionId;
    }

    public Integer getPreviousExamId() {
        return previousExamId;
    }

    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    public Integer getPreviousSessionId() {
        return previousSessionId;
    }

    public void setPreviousSessionId(Integer previousSessionId) {
        this.previousSessionId = previousSessionId;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public Integer getSelectedSessionId() {
        return selectedSessionId;
    }

    public void setSelectedSessionId(Integer selectedSessionId) {
        this.selectedSessionId = selectedSessionId;
    }

    public Integer getLoadedExamId() {
        return loadedExamId;
    }

    public void setLoadedExamId(Integer loadedExamId) {
        this.loadedExamId = loadedExamId;
    }

    public Integer getLoadedSessionId() {
        return loadedSessionId;
    }

    public void setLoadedSessionId(Integer loadedSessionId) {
        this.loadedSessionId = loadedSessionId;
    }

    public String getExamIdParam() {
        return examIdParam;
    }

    public void setExamIdParam(String examIdParam) {
        this.examIdParam = examIdParam;
    }

    public boolean isHasSessionIdParam() {
        return hasSessionIdParam;
    }

    public void setHasSessionIdParam(boolean hasSessionIdParam) {
        this.hasSessionIdParam = hasSessionIdParam;
    }

    public boolean isLoadCandidates() {
        return loadCandidates;
    }

    public void setLoadCandidates(boolean loadCandidates) {
        this.loadCandidates = loadCandidates;
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

    public List<ExamRegistrationDTO> getCachedQueue() {
        return cachedQueue;
    }

    public void setCachedQueue(List<ExamRegistrationDTO> cachedQueue) {
        this.cachedQueue = cachedQueue;
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
