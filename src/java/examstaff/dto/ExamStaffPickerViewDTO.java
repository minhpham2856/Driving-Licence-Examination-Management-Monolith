package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * View-model bộ chọn kỳ thi (exam picker) trên các trang ExamStaff.
 *
 * Vai trò:
 * Cung cấp options, allExams, currentExam và id đã commit để binder đổ vào JSP
 * (dropdown / sidebar chọn kỳ).
 *
 * Ai tạo / tiêu thụ:
 * ExamStaffPageServiceImpl gắn vào ExamStaffPageContext.getPickerView();
 * ExamStaffPageBinder bind attribute trên hầu hết trang staff.
 */
public class ExamStaffPickerViewDTO {

    private List<ExamSummaryDTO> examOptions;
    private List<ExamSummaryDTO> allExams;
    private ExamSummaryDTO currentExam;
    private int examId;
    private Integer selectedExamId;
    private Integer pickerCommittedExamId;

    /** Danh sách kỳ hiển thị trong dropdown picker (có thể đã lọc). */
    public List<ExamSummaryDTO> getExamOptions() {
        return examOptions;
    }

    /** Gán options picker. */
    public void setExamOptions(List<ExamSummaryDTO> examOptions) {
        this.examOptions = examOptions;
    }

    /** Toàn bộ kỳ thi staff truy cập được. */
    public List<ExamSummaryDTO> getAllExams() {
        return allExams;
    }

    /** Gán danh sách đầy đủ kỳ thi. */
    public void setAllExams(List<ExamSummaryDTO> allExams) {
        this.allExams = allExams;
    }

    /** Kỳ thi đang chọn / đang làm việc trên UI. */
    public ExamSummaryDTO getCurrentExam() {
        return currentExam;
    }

    /** Gán kỳ hiện tại cho picker. */
    public void setCurrentExam(ExamSummaryDTO currentExam) {
        this.currentExam = currentExam;
    }

    /** ExamId hiện tại (ngắn gọn cho form hidden / so sánh). */
    public int getExamId() {
        return examId;
    }

    /** Gán examId hiện tại. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** ExamId user đang chọn trên control (trước khi commit). */
    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    /** Gán examId đang chọn trên UI. */
    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    /** ExamId đã commit vào session sau select-exam thành công. */
    public Integer getPickerCommittedExamId() {
        return pickerCommittedExamId;
    }

    /** Gán examId đã commit. */
    public void setPickerCommittedExamId(Integer pickerCommittedExamId) {
        this.pickerCommittedExamId = pickerCommittedExamId;
    }
}
