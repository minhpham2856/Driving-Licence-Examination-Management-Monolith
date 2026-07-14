package examstaff.util;

/** Chuẩn hóa tham số lọc audit (key ngày). */
public final class AuditFilterHelper {

    private AuditFilterHelper() {
    }

    /**
     * Trim filter ngày; null → chuỗi rỗng.
     *
     * @param filterDate chuỗi ngày lọc
     * @return key đã chuẩn hóa (không null)
     */
    public static String normalizeFilterKey(String filterDate) {
        return filterDate == null ? "" : filterDate.trim();
    }
}
