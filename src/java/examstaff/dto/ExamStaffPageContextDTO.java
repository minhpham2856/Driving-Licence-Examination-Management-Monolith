package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

/**
 * Ngữ cảnh trang ExamStaff sau khi chuẩn bị: kỳ thi hiện tại, danh sách kỳ và thí sinh, picker.
 */
public class ExamStaffPageContextDTO {

    private int examId;
    private List<ExamSummaryDTO> allExams;
    private List<ExamRegistrationDTO> candidates;
    private ExamStaffPickerViewDTO pickerView;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public List<ExamSummaryDTO> getAllExams() {
        return allExams;
    }

    public void setAllExams(List<ExamSummaryDTO> allExams) {
        this.allExams = allExams;
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
