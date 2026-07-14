package registrant.enums;

/**
 * Trạng thái hồ sơ tài liệu trên bảng ExamRegistration (giai đoạn trước / song song đăng ký ca thi).
 * Khác với PreRegistered, CheckedIn… dùng khi thí sinh đã ghi danh ca thi cụ thể.
 */
public final class ProfileRegistrationStatus {

    public static final String DRAFT = "Draft";
    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";

    private ProfileRegistrationStatus() {
    }

    /** Mệnh đề SQL IN — hỗ trợ cả mã EN (portal) và VN (DML seed). */
    public static final String SQL_IN_WORKFLOW =
            "N'Draft', N'Pending', N'Approved', N'Rejected', N'Chờ duyệt', N'Duyệt', N'Loại'";

    public static boolean isDocumentWorkflowStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String s = status.trim();
        return DRAFT.equalsIgnoreCase(s)
                || PENDING.equalsIgnoreCase(s)
                || APPROVED.equalsIgnoreCase(s)
                || REJECTED.equalsIgnoreCase(s)
                || "Chờ duyệt".equalsIgnoreCase(s)
                || "Duyệt".equalsIgnoreCase(s)
                || "Loại".equalsIgnoreCase(s);
    }

    public static String toDisplayLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Chưa có hồ sơ";
        }
        return switch (status.trim()) {
            case DRAFT -> "Đang bổ sung hồ sơ";
            case PENDING -> "Chờ ban quản lý duyệt";
            case APPROVED -> "Đã duyệt hồ sơ";
            case REJECTED -> "Bị từ chối — cần bổ sung";
            default -> status.trim();
        };
    }

    public static String toBadgeClass(String status) {
        if (status == null || status.isBlank()) {
            return "gray";
        }
        return switch (status.trim()) {
            case APPROVED -> "success";
            case PENDING -> "pending";
            case REJECTED -> "danger";
            case DRAFT -> "warning";
            default -> "info";
        };
    }
}
