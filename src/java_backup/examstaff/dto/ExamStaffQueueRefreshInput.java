package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

public class ExamStaffQueueRefreshInput {

    private int examId;
    private String webRoot;
    private List<ExamSummaryDTO> allSessions;
    private Integer selectedExamId;
    private List<String> callQueueOrder;
    private Integer callQueueOrderExamId;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
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

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
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
