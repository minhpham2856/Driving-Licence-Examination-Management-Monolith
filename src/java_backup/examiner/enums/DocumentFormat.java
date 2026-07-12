package examiner.enums;

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
        for (DocumentFormat format : values()) {
            if (format.getValue().equalsIgnoreCase(value.trim())) {
                return format;
            }
        }
        return null;
    }
}
