package examstaff.dto;

import dto.ExamSummaryDTO;

import java.util.List;

public class ExamStaffPickerViewDTO {

    private List<ExamSummaryDTO> examOptions;
    private List<ExamSummaryDTO> allSessions;
    private ExamSummaryDTO currentSession;
    private int examId;
    private Integer selectedExamId;
    private Integer pickerCommittedExamId;

    public List<ExamSummaryDTO> getExamOptions() {
        return examOptions;
    }

    public void setExamOptions(List<ExamSummaryDTO> examOptions) {
        this.examOptions = examOptions;
    }

    public List<ExamSummaryDTO> getAllSessions() {
        return allSessions;
    }

    public void setAllSessions(List<ExamSummaryDTO> allSessions) {
        this.allSessions = allSessions;
    }

    public ExamSummaryDTO getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(ExamSummaryDTO currentSession) {
        this.currentSession = currentSession;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public Integer getPickerCommittedExamId() {
        return pickerCommittedExamId;
    }

    public void setPickerCommittedExamId(Integer pickerCommittedExamId) {
        this.pickerCommittedExamId = pickerCommittedExamId;
    }
}
