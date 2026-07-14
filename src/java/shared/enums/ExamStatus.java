package shared.enums;

public enum ExamStatus {
    NOT_STARTED("Chưa diễn ra"),
    IN_PROGRESS("Đang diễn ra"),
    PAUSED("Tạm dừng"),
    COMPLETED("Hoàn tất"),
    CANCELLED("Đã hủy");

    private final String value;

    private ExamStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
