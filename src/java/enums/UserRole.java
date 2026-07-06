package enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    EXAMINER("Sát hạch viên"),
    MANAGING_STAFF("Cán bộ quản lý"),
    EXAM_STAFF("Cán bộ kỳ thi"),
    CANDIDATE("Thí sinh"),
    REGISTRANT("Người đăng ký thi");

    private final String value;

    private UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
