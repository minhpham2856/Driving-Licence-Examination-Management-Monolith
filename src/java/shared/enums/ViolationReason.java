package shared.enums;

public enum ViolationReason {
    CHEATING("Gian lận"),
    SAFETY_VIOLATION("Vi phạm nghiêm trọng"),
    OTHER("Khác");

    private final String value;

    private ViolationReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ViolationReason fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ViolationReason reason : values()) {
            if (reason.getValue().equals(value)) {
                return reason;
            }
        }
        return null;
    }
}
