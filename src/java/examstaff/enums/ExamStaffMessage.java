package examstaff.enums;

public enum ExamStaffMessage {
    EXAM_SELECTED("Đã chọn kỳ thi mới."),
    EXAM_NOT_FOUND_PREFIX("Không tìm thấy kỳ thi"),
    EXAM_CHANGE_ERROR_PREFIX("Không đổi được kỳ thi: "),
    UNKNOWN_ERROR("lỗi không xác định");

    private final String text;

    ExamStaffMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String formatExamNotFound(String param) {
        if (param == null || param.isBlank()) {
            return EXAM_NOT_FOUND_PREFIX.getText() + ".";
        }
        return EXAM_NOT_FOUND_PREFIX.getText() + " (id=" + param + ").";
    }
}
