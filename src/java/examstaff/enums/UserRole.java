package examstaff.enums;

/** Vai trò dùng trong examstaff (lọc sát hạch viên). */
public enum UserRole {
    /** Sát hạch viên. */
    SAT_HACH_VIEN("Sát hạch viên");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
