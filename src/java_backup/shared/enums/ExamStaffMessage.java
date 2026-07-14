package shared.enums;

public enum ExamStaffMessage {
    SESSION_SELECTED("ÄÃ£ chá»n ká»³ thi má»›i."),
    EXAM_NOT_FOUND_PREFIX("KhÃ´ng tÃ¬m tháº¥y ká»³ thi"),
    SESSION_CHANGE_ERROR_PREFIX("KhÃ´ng Ä‘á»•i Ä‘Æ°á»£c ká»³ thi: "),
    UNKNOWN_ERROR("lá»—i khÃ´ng xÃ¡c Ä‘á»‹nh"),
    PHOTO_REQUIRED("KhÃ´ng thá»ƒ thu lá»‡ phÃ­: thÃ­ sinh chÆ°a chá»¥p áº£nh chÃ¢n dung táº¡i bÃ n thá»§ tá»¥c."),
    PAYMENT_WRITE_FAILED("KhÃ´ng ghi Ä‘Æ°á»£c thanh toÃ¡n. Vui lÃ²ng thá»­ láº¡i.");

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

