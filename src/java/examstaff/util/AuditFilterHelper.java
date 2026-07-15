package examstaff.util;

/**
 * Chuẩn hóa tham số lọc audit (key ngày) trước khi so khớp / cache.
 */
public final class AuditFilterHelper {

    /** Không cho khởi tạo — chỉ dùng static. */
    private AuditFilterHelper() {
    }

    /**
     * Trim filter ngày; null → chuỗi rỗng (không bao giờ trả null).
     *
     * @param filterDate chuỗi ngày lọc (có thể null hoặc khoảng trắng)
     * @return key đã chuẩn hóa (không null)
     */
    public static String normalizeFilterKey(String filterDate) {
        // null → key rỗng; còn lại bỏ khoảng đầu/cuối
        return filterDate == null ? "" : filterDate.trim();
    }
}
