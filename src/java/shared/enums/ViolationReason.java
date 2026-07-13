package shared.enums;

public enum ViolationReason {
    SAFETY_VIOLATION("Gây mất an toàn nghiêm trọng trong quá trình thi"),
    CHEATING("Gian lận"),
    OTHER("Lý do khác");

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
