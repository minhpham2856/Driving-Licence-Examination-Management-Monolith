package examstaff.enums;

/**
 * Vai trò dùng trong module examstaff (lọc danh sách sát hạch viên khi phân công).
 * Khác với {@code shared.enums.RoleType} — chỉ chứa nhãn hiển thị nội bộ exam staff.
 */
public enum UserRole {
    /** Sát hạch viên — người được phân công coi / chấm tại phòng/sân thi. */
    SAT_HACH_VIEN("Sát hạch viên");

    /** Nhãn tiếng Việt hiển thị trên UI / báo cáo. */
    private final String displayName;

    /**
     * Gán nhãn hiển thị cho hằng enum.
     *
     * @param displayName tên tiếng Việt
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy nhãn tiếng Việt của vai trò.
     *
     * @return display name (ví dụ {@code "Sát hạch viên"})
     */
    public String getDisplayName() {
        return displayName;
    }
}
