package shared.enums;

public enum RegistrationType {
    PRE_REGISTERED("PreRegistered"),
    RETAKE("Retake");

    private final String value;

    private RegistrationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RegistrationType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RegistrationType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
