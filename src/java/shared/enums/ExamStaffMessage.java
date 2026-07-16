//package shared.enums;
//
//public enum ExamStaffMessage {
//    SESSION_SELECTED("Đã chọn kỳ thi mới."),
//    EXAM_NOT_FOUND_PREFIX("Không tìm thấy kỳ thi"),
//    SESSION_CHANGE_ERROR_PREFIX("Không đổi được kỳ thi: "),
//    UNKNOWN_ERROR("lỗi không xác định"),
//    PHOTO_REQUIRED("Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục."),
//    PAYMENT_WRITE_FAILED("Không ghi được thanh toán. Vui lòng thử lại.");
//
//    private final String value;
//
//    ExamStaffMessage(String value) {
//        this.value = value;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public static ExamStaffMessage fromValue(String value) {
//        if (value == null) {
//            return null;
//        }
//        for (ExamStaffMessage status : values()) {
//            if (status.getValue().equals(value)) {
//                return status;
//            }
//        }
//        return null;
//    }
//}
