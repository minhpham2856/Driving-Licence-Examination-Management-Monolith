package examstaff.enums;

public enum ExamStaffMessage {
    SESSION_SELECTED("Đã chọn kỳ thi mới."),
    EXAM_NOT_FOUND_PREFIX("Không tìm thấy kỳ thi"),
    SESSION_CHANGE_ERROR_PREFIX("Không đổi được kỳ thi: "),
    UNKNOWN_ERROR("lỗi không xác định"),
    PHOTO_REQUIRED("Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục."),
    PAYMENT_WRITE_FAILED("Không ghi được thanh toán. Vui lòng thử lại.");

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

    public String formatSessionChangeError(String detail) {
        String msg = detail != null && !detail.isBlank() ? detail : UNKNOWN_ERROR.getText();
        return SESSION_CHANGE_ERROR_PREFIX.getText() + msg;
    }
}
