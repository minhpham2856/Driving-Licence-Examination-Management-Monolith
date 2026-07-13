package shared.enums;

public enum SectionType {
    THEORY("Lý thuyết"),
    LAYOUT("Thực hành trong hình");

    private final String value;

    private SectionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SectionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SectionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
