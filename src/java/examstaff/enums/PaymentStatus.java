package examstaff.enums;

/**
 * Trạng thái thanh toán lệ phí (giá trị hiển thị / khớp chuỗi CSDL).
 */
public enum PaymentStatus {
    /** Đã hoàn tất thanh toán (VI). */
    HOAN_TAT("Hoàn tất"),
    /** Chờ thanh toán. */
    CHO_THANH_TOAN("Chờ thanh toán"),
    /** Đồng nghĩa hoàn tất (EN — legacy DB). */
    COMPLETED("Completed"),
    /** Đồng nghĩa đã thanh toán (EN — legacy DB). */
    PAID("Paid");
    private final String displayName;
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    /** So khớp chuỗi trạng thái thanh toán. */
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }
    /** Chuẩn hóa chuỗi về enum; mặc định chờ thanh toán. */
    public static PaymentStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return CHO_THANH_TOAN;
        }
        String trimmed = value.trim();
        for (PaymentStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return CHO_THANH_TOAN;
    }
    /** Đã thanh toán xong (một trong các giá trị hoàn tất). */
    public static boolean isCompleted(String value) {
        PaymentStatus status = normalize(value);
        return status == HOAN_TAT || status == COMPLETED || status == PAID;
    }

    /** Giá trị PaymentStatus hợp lệ khi ghi/đọc CSDL. */
    public static String sqlInClause() {
        return "N'" + HOAN_TAT.displayName + "', N'" + COMPLETED.displayName + "', N'" + PAID.displayName + "'";
    }
}
