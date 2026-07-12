package shared.enums;

public enum DocumentFormat {
    EXCEL("excel"),
    XML("xml"),
    DOCX("docx");

    private final String value;

    private DocumentFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DocumentFormat fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DocumentFormat status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
