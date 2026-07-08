package enums;

public enum PaymentMethod {
    CASH("Cash");

    private final String code;

    PaymentMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
