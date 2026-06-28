package enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    EXAMINER("Sát hạch viên"),
    MANAGING_STAFF("Cán bộ quản lý"),
    EXAM_STAFF("Cán bộ kỳ thi"),
    CANDIDATE("Thí sinh"),
    REGISTRANT("Người đăng ký");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
