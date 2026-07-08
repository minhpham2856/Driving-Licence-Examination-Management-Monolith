package dto.examstaff;

import dto.SessionDTO;

import java.util.List;

public class ExamStaffSelectionResolveInput {

    private int urlSessionId;
    private String examIdParam;
    private Integer selectedExamId;
    private Integer selectedSessionId;
    private List<SessionDTO> allSessions;
    private int defaultExamId;
    private int defaultSessionId;

    public int getUrlSessionId() {
        return urlSessionId;
    }

    public void setUrlSessionId(int urlSessionId) {
        this.urlSessionId = urlSessionId;
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

    public Integer getSelectedSessionId() {
        return selectedSessionId;
    }

    public void setSelectedSessionId(Integer selectedSessionId) {
        this.selectedSessionId = selectedSessionId;
    }

    public List<SessionDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<SessionDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public int getDefaultExamId() {
        return defaultExamId;
    }

    public void setDefaultExamId(int defaultExamId) {
        this.defaultExamId = defaultExamId;
    }

    public int getDefaultSessionId() {
        return defaultSessionId;
    }

    public void setDefaultSessionId(int defaultSessionId) {
        this.defaultSessionId = defaultSessionId;
    }
}
