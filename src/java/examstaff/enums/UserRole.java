package examstaff.enums;

/**
 * Enum vai trò nghiệp vụ nội bộ module ExamStaff — nhãn hiển thị cho sát hạch viên
 * khi phân công coi thi. Khác {@code shared.enums.RoleType} (auth toàn hệ thống).
 *
 * Vai trò trong luồng examstaff:
 * Hiện chỉ có {@link #SAT_HACH_VIEN} — người được phân công tại phòng lý thuyết hoặc sân thực hành.
 * Dùng làm nhãn UI/báo cáo phân công giám thị, không thay thế kiểm tra quyền đăng nhập
 * ({@code RoleType.EXAM_STAFF} trên {@code ExamStaffSidebarFilter}).
 *
 * Giá trị hiện có:
 * - {@link #SAT_HACH_VIEN} — {@code displayName = "Sát hạch viên"}.
 *
 * Ai sử dụng:
 * {@code ExaminerAssignmentDAOImpl}, {@code ExaminerAllocationServiceImpl},
 * {@code ExaminerAssignmentRules} — lọc và hiển thị danh sách giám khảo/sát hạch viên theo ca.
 */
public enum UserRole {
    /** Sát hạch viên — người được phân công coi / chấm tại phòng/sân thi. */
    SAT_HACH_VIEN("Sát hạch viên");

    /** Nhãn tiếng Việt hiển thị trên UI / báo cáo. */
    private final String displayName;

    /**
     * Gán nhãn hiển thị cho hằng enum.
     * @param displayName tên tiếng Việt
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy nhãn tiếng Việt của vai trò.
     * @return display name (ví dụ {@code "Sát hạch viên"})
     */
    public String getDisplayName() {
        return displayName;
    }
}
