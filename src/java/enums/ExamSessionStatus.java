package enums;

public enum ExamSessionStatus {
    NOT_STARTED("Chưa diễn ra"),
    IN_PROGRESS("Đang diễn ra"),
    COMPLETED("Hoàn tất"),
    CANCELLED("Đã hủy");

    private final String value;

    private ExamSessionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamSessionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamSessionStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static boolean isEnded(String value) {
        ExamSessionStatus status = fromValue(value);
        return status == COMPLETED || status == CANCELLED;
    }
}
