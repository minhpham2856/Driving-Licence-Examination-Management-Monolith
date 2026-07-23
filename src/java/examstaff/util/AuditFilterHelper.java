package examstaff.util;

/**
 * Utility chuẩn hóa tham số lọc ngày trên màn audit ExamStaff — tạo cache key ổn định
 * trước khi query hoặc so khớp bộ lọc.
 *
 * Vai trò trong luồng examstaff:
 * {@link AuditServlet} và {@code StaffAuditPageServiceImpl} nhận {@code filterDate} từ query string;
 * helper trim và coi null thành chuỗi rỗng để tránh NPE và key cache không nhất quán
 * (ví dụ {@code " 2026-07-21 "} vs {@code "2026-07-21"}).
 *
 * Cách hoạt động:
 * {@link #normalizeFilterKey} — null → {@code ""}; còn lại {@code trim()}.
 * Lớp {@code final} với constructor private; chỉ static API.
 *
 * Ai gọi:
 * {@code StaffAuditPageServiceImpl}, {@code AuditServlet}, {@code AuditLogDAOImpl} —
 * mọi nơi bind hoặc đọc tham số ngày lọc nhật ký audit.
 */
public final class AuditFilterHelper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private AuditFilterHelper() {
    }

    /**
     * Trim filter ngày; null → chuỗi rỗng (không bao giờ trả null).
     * @param filterDate chuỗi ngày lọc (có thể null hoặc khoảng trắng)
     * @return key đã chuẩn hóa (không null)
     */
    public static String normalizeFilterKey(String filterDate) {
        // null → key rỗng; còn lại bỏ khoảng đầu/cuối
        return filterDate == null ? "" : filterDate.trim();
    }
}
