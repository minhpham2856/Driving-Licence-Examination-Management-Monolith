package examstaff.enums;
public enum PaymentStatus {
    HOAN_TAT("Hoàn tất"),
    CHO_THANH_TOAN("Chờ thanh toán"),
    COMPLETED("Completed"),
    PAID("Paid");
    private final String displayName;
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }
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
    public static boolean isCompleted(String value) {
        PaymentStatus status = normalize(value);
        return status == HOAN_TAT || status == COMPLETED || status == PAID;
    }

    /** Giá trị PaymentStatus hợp lệ khi ghi/đọc CSDL. */
    public static String sqlInClause() {
        return "N'" + HOAN_TAT.displayName + "', N'" + COMPLETED.displayName + "', N'" + PAID.displayName + "'";
    }
}
