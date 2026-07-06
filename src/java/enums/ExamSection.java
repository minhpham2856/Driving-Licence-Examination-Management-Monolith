package enums;

public enum ExamSection {
    THEORY("Lý thuyết"),
    LAYOUT("Thực hành trong hình"),
    ROAD("Thực hành trên đường");

    private final String value;

    private ExamSection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamSection fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamSection section : values()) {
            if (section.getValue().equals(value)) {
                return section;
            }
        }
        return null;
    }
}
