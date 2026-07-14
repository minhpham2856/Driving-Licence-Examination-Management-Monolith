package shared.enums;

public enum RoleType {
    ADMIN("Quản trị viên"),
    EXAMINER("Sát hạch viên"),
    MANAGING_STAFF("Cán bộ quản lý"),
    EXAM_STAFF("Cán bộ kỳ thi"),
    CANDIDATE("Thí sinh"),
    REGISTRANT("Người đăng ký thi");

    private final String value;

    private RoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RoleType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
