package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * View-model bộ chọn kỳ thi (exam picker) trên các trang ExamStaff.
 */
public class ExamStaffPickerViewDTO {

    private List<ExamSummaryDTO> examOptions;
    private List<ExamSummaryDTO> allExams;
    private ExamSummaryDTO currentExam;
    private int examId;
    private Integer selectedExamId;
    private Integer pickerCommittedExamId;

    public List<ExamSummaryDTO> getExamOptions() {
        return examOptions;
    }

    public void setExamOptions(List<ExamSummaryDTO> examOptions) {
        this.examOptions = examOptions;
    }

    public List<ExamSummaryDTO> getAllExams() {
        return allExams;
    }

    public void setAllExams(List<ExamSummaryDTO> allExams) {
        this.allExams = allExams;
    }

    public ExamSummaryDTO getCurrentExam() {
        return currentExam;
    }

    public void setCurrentExam(ExamSummaryDTO currentExam) {
        this.currentExam = currentExam;
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
