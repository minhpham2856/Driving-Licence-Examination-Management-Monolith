package dto.examstaff;

import dto.SessionDTO;

import java.util.List;

public class ExamStaffPageTransitionInput {

    private int urlSessionId;
    private Integer previousExamId;
    private Integer previousSessionId;
    private Integer loadedSessionId;
    private List<SessionDTO> allSessions;

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

    public Integer getLoadedSessionId() {
        return loadedSessionId;
    }

    public void setLoadedSessionId(Integer loadedSessionId) {
        this.loadedSessionId = loadedSessionId;
    }

    public List<SessionDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<SessionDTO> allSessions) {
        this.allSessions = allSessions;
    }
}
