package enums;

public enum PaymentStatus {
    COMPLETED("Hoàn tất"),
    FAILED("Thất bại"),
    PENDING("Chờ thanh toán");

    private final String value;

    private PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
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
}
