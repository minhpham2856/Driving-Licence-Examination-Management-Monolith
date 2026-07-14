package shared.enums;

public enum ErrorType {
    VALIDATION_FAILED("VALIDATION_FAILED"),
    NOT_FOUND("NOT_FOUND"),
    NOT_CONFIGURED("NOT_CONFIGURED"),
    PERMISSION_DENIED("PERMISSION_DENIED"),
    PERSISTENCE_FAILED("PERSISTENCE_FAILED"),
    EXTERNAL_SERVICE_FAILED("EXTERNAL_SERVICE_FAILED"),
    NOT_IMPLEMENTED("NOT_IMPLEMENTED");

    private final String value;

    private ErrorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ErrorType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ErrorType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
