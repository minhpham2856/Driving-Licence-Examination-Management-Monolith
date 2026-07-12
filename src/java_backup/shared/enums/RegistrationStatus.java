package shared.enums;

public enum RegistrationStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Duyệt"),
    REJECTED("Loại");

    private final String value;

    private RegistrationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RegistrationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RegistrationStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
