package shared.enums;

/** Thông điệp cố định UI ExamStaff (chọn kỳ thi / lỗi). */
public enum ExamStaffMessage {
    /** Thông báo đã chọn kỳ thi mới. */
    EXAM_SELECTED("Đã chọn kỳ thi mới."),
    /** Tiền tố khi không tìm thấy kỳ thi. */
    EXAM_NOT_FOUND_PREFIX("Không tìm thấy kỳ thi"),
    /** Tiền tố lỗi khi đổi kỳ thi. */
    EXAM_CHANGE_ERROR_PREFIX("Không đổi được kỳ thi: "),
    /** Kỳ đã kết thúc - không sửa hồ sơ / đình chỉ / hoàn tác. */
    EXAM_MUTATIONS_LOCKED(
            "Kỳ thi đã kết thúc. Không thể xóa, sửa hồ sơ, đình chỉ hoặc hoàn tác đình chỉ."),
    /** Mô tả lỗi mặc định. */
    UNKNOWN_ERROR("lỗi không xác định");

    private final String text;

    ExamStaffMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /** Ghép thông báo không tìm thấy kỳ thi (có thể kèm id). */
    public String formatExamNotFound(String param) {
        if (param == null || param.isBlank()) {
            return EXAM_NOT_FOUND_PREFIX.getText() + ".";
        }
        return EXAM_NOT_FOUND_PREFIX.getText() + " (id=" + param + ").";
    }
}
