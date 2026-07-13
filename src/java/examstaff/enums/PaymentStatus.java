package examstaff.enums;

public enum PaymentStatus {
    COMPLETED("Hoàn tất"),
    FAILED("Thất bại"),
    PENDING("Chờ thanh toán"),
    HOAN_TAT("Hoàn tất"),
    CHO_THANH_TOAN("Chờ thanh toán"),
    PAID("Paid");

    private final String value;

    private PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public boolean matches(String val) {
        if (val == null || val.isBlank()) {
            return false;
        }
        return value.equalsIgnoreCase(val.trim());
    }

    public static PaymentStatus normalize(String val) {
        if (val == null || val.isBlank()) {
            return PENDING;
        }
        String trimmed = val.trim();
        for (PaymentStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return PENDING;
    }

    public static boolean isCompleted(String val) {
        PaymentStatus status = normalize(val);
        return status == COMPLETED || status == HOAN_TAT || status == PAID;
    }

    public static String sqlInClause() {
        return "N'" + COMPLETED.value + "', N'" + HOAN_TAT.value + "', N'" + PAID.value + "'";
    }
}