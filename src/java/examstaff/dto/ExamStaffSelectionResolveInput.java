package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

public class ExamStaffSelectionResolveInput {

    private int urlExamId;
    private String examIdParam;
    private Integer selectedExamId;
    private List<ExamSummaryDTO> allSessions;
    private int defaultExamId;

    public int getUrlExamId() {
        return urlExamId;
    }

    public void setUrlExamId(int urlExamId) {
        this.urlExamId = urlExamId;
    }

    public String getExamIdParam() {
        return examIdParam;
    }

    public void setExamIdParam(String examIdParam) {
        this.examIdParam = examIdParam;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public List<ExamSummaryDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<ExamSummaryDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public int getDefaultExamId() {
        return defaultExamId;
    }

    public void setDefaultExamId(int defaultExamId) {
        this.defaultExamId = defaultExamId;
    }
}
