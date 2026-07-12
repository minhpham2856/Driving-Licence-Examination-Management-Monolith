package shared.enums;

public enum AuditEntity {
    CANDIDATE("Thí sinh"),
    EXAM_RESULT("Kết quả thi"),
    EXAM_ROOM("Phòng thi"),
    EXAM_DEVICE("Thiết bị thi"),
    EXAM_SESSION("Ca thi"),
    EXAMINER_ASSIGNMENT("Phân công sát hạch viên"),
    CANDIDATE_CALL("Gọi thí sinh"),
    DOSSIER("Hồ sơ"),
    PAYMENT("Thanh toán"),
    EXAM_SCORE("Điểm thi");

    private final String value;

    private AuditEntity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuditEntity fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AuditEntity status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
