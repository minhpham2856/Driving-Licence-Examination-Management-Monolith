package shared.enums;

public enum FileType {
    EXCEL("excel"),
    DOCX("docx");

    private final String value;

    private FileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FileType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (FileType format : values()) {
            if (format.getValue().equalsIgnoreCase(value.trim())) {
                return format;
            }
        }
        return null;
    }
}
