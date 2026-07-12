package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ExamStaffPageContextDTO {

    private int examId;
    private List<ExamSummaryDTO> allSessions;
    private List<ExamRegistrationDTO> candidates;
    private ExamStaffPickerViewDTO pickerView;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public List<ExamSummaryDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<ExamSummaryDTO> allSessions) {
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
