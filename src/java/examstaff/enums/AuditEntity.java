package examstaff.enums;

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
        for (AuditEntity entity : values()) {
            if (entity.getValue().equals(value) || entity.name().equalsIgnoreCase(value)) {
                return entity;
            }
        }
        return null;
    }
    
    public static String resolveLabel(String tableName) {
        if (tableName == null) return "Bản ghi";
        AuditEntity entity = fromValue(tableName);
        if (entity != null) return entity.getValue();
        return tableName;
    }
}
