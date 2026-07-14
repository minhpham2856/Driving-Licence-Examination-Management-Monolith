package shared.enums;

public enum ExamSection {
    LY_THUYET("Lý thuyết"),
    THUC_HANH_TRONG_HINH("Thực hành trong hình");

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
        for (ExamSection status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
