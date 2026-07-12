package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

public class ExamStaffPageTransitionInput {

    private int urlExamId;
    private Integer previousExamId;
    private Integer loadedExamId;
    private List<ExamSummaryDTO> allSessions;

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

    public Integer getLoadedExamId() {
        return loadedExamId;
    }

    public void setLoadedExamId(Integer loadedExamId) {
        this.loadedExamId = loadedExamId;
    }

    public List<ExamSummaryDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<ExamSummaryDTO> allSessions) {
        this.allSessions = allSessions;
    }
}
