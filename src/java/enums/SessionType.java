package enums;

public enum SessionType {
    MORNING("Ca sáng"),
    AFTERNOON("Ca chiều");

    private final String value;

    SessionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SessionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SessionType ca : values()) {
            if (ca.getValue().equals(value)) {
                return ca;
            }
        }
        return null;
    }
}
