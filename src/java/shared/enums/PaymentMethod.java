package shared.enums;

public enum PaymentMethod {
    CASH("Cash");

    private final String value;

    private PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentMethod fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentMethod status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
