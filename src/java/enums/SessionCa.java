package enums;

public enum SessionCa {
    MORNING("Ca sáng"),
    AFTERNOON("Ca chiều");

    private final String value;

    SessionCa(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SessionCa fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SessionCa ca : values()) {
            if (ca.getValue().equals(value)) {
                return ca;
            }
        }
        return null;
    }
}
