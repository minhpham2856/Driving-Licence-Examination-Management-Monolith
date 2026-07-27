package examstaff.enums;

/**
 * Enum vai trò nghiệp vụ nội bộ module ExamStaff — nhãn hiển thị cho sát hạch viên
 * khi phân công coi thi. Khác shared.enums.RoleType (auth toàn hệ thống).
 *
 * Vai trò trong luồng examstaff:
 * Hiện chỉ có SAT_HACH_VIEN — người được phân công tại phòng lý thuyết hoặc sân thực hành.
 * Dùng làm nhãn UI/báo cáo phân công giám thị, không thay thế kiểm tra quyền đăng nhập
 * (RoleType.EXAM_STAFF trên ExamStaffSidebarFilter).
 *
 * Giá trị hiện có:
 * - SAT_HACH_VIEN — displayName = "Sát hạch viên".
 *
 * Ai sử dụng:
 * ExaminerAssignmentDAOImpl, ExaminerAllocationServiceImpl,
 * ExaminerAssignmentRules — lọc và hiển thị danh sách sát hạch viên theo ca.
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
     * @return display name (ví dụ "Sát hạch viên")
     */
    public String getDisplayName() {
        return displayName;
    }
}
