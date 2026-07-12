package examstaff.dto;

import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ExamStaffPagePrepareInput {

    private int urlExamId;
    private Integer previousExamId;
    private Integer selectedExamId;
    private Integer loadedExamId;
    private String examIdParam;
    private boolean hasExamIdParam;
    private boolean loadCandidates;
    private String webRoot;
    private List<ExamSummaryDTO> allSessions;
    private List<ExamRegistrationDTO> cachedQueue;
    private List<String> callQueueOrder;
    private Integer callQueueOrderExamId;

    public int getUrlExamId() {
        return urlExamId;
    }

    public void setUrlExamId(int urlExamId) {
        this.urlExamId = urlExamId;
    }

    public Integer getPreviousExamId() {
        return previousExamId;
    }

    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public Integer getLoadedExamId() {
        return loadedExamId;
    }

    public void setLoadedExamId(Integer loadedExamId) {
        this.loadedExamId = loadedExamId;
    }

    public String getExamIdParam() {
        return examIdParam;
    }

    public void setExamIdParam(String examIdParam) {
        this.examIdParam = examIdParam;
    }

    public boolean isHasExamIdParam() {
        return hasExamIdParam;
    }

    public void setHasExamIdParam(boolean hasExamIdParam) {
        this.hasExamIdParam = hasExamIdParam;
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

    public List<ExamSummaryDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<ExamSummaryDTO> allSessions) {
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

    public Integer getCallQueueOrderExamId() {
        return callQueueOrderExamId;
    }

    public void setCallQueueOrderExamId(Integer callQueueOrderExamId) {
        this.callQueueOrderExamId = callQueueOrderExamId;
    }
}
