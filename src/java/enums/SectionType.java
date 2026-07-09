package enums;

public enum SectionType {
    THEORY("Lý thuyết"),
    LAYOUT("Thực hành trong hình"),
    ROAD("Thực hành trên đường");

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
        for (SectionType section : values()) {
            if (section.getValue().equals(value)) {
                return section;
            }
        }
        return null;
    }
}
