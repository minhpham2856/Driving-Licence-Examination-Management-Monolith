package shared.enums;

public enum SectionStatus {
    CHUA_THI("Chưa thi"),
    DANG_THI("Đang thi"),
    DAT("Đạt"),
    TRUOT("Trượt"),
    BO_THI("Bỏ thi");

    private final String value;

    private SectionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SectionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SectionStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
