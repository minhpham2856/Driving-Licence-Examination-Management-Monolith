package shared.enums;

public enum CandidateStatus {
    NOT_STARTED("Chưa thi"),
    IN_PROGRESS("Đang thi"),
    AWAITING_SIGNATURE("Chờ ký"),
    COMPLETED("Đã thi");

    private final String value;

    private CandidateStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CandidateStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CandidateStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
