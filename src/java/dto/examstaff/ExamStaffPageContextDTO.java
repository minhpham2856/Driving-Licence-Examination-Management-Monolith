package dto.examstaff;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ExamStaffPageContextDTO {

    private int examId;
    private int sessionId;
    private List<SessionDTO> allSessions;
    private List<ExamRegistrationDTO> candidates;
    private ExamStaffPickerViewDTO pickerView;

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

    public List<SessionDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<SessionDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public List<ExamRegistrationDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<ExamRegistrationDTO> candidates) {
        this.candidates = candidates;
    }

    public ExamStaffPickerViewDTO getPickerView() {
        return pickerView;
    }

    public void setPickerView(ExamStaffPickerViewDTO pickerView) {
        this.pickerView = pickerView;
    }
}
