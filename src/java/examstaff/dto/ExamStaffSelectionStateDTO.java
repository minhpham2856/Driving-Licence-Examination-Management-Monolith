package examstaff.dto;

/**
 * Trạng thái kỳ thi đã chọn sau khi resolve (examId).
 */
public class ExamStaffSelectionStateDTO {

    private int examId;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }
}
