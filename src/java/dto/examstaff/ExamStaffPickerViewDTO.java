package dto.examstaff;

import dto.SessionDTO;

import java.util.List;

public class ExamStaffPickerViewDTO {

    private List<SessionDTO> examOptions;
    private List<SessionDTO> allSessions;
    private SessionDTO currentSession;
    private int examId;
    private Integer selectedSessionId;
    private Integer pickerCommittedSessionId;
    private Integer pickerCommittedExamId;

    public List<SessionDTO> getExamOptions() {
        return examOptions;
    }

    public void setExamOptions(List<SessionDTO> examOptions) {
        this.examOptions = examOptions;
    }

    public List<SessionDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<SessionDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public SessionDTO getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(SessionDTO currentSession) {
        this.currentSession = currentSession;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public Integer getSelectedSessionId() {
        return selectedSessionId;
    }

    public void setSelectedSessionId(Integer selectedSessionId) {
        this.selectedSessionId = selectedSessionId;
    }

    public Integer getPickerCommittedSessionId() {
        return pickerCommittedSessionId;
    }

    public void setPickerCommittedSessionId(Integer pickerCommittedSessionId) {
        this.pickerCommittedSessionId = pickerCommittedSessionId;
    }

    public Integer getPickerCommittedExamId() {
        return pickerCommittedExamId;
    }

    public void setPickerCommittedExamId(Integer pickerCommittedExamId) {
        this.pickerCommittedExamId = pickerCommittedExamId;
    }
}
