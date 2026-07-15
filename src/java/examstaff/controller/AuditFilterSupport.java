package examstaff.controller;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Helper đọc/chuẩn hóa filter ngày cho trang nhật ký audit.
 */
public final class AuditFilterSupport {

    private AuditFilterSupport() {
    }

    /**
     * Đọc {@code filterDate} (ưu tiên) hoặc {@code date} từ request; có thể null/blank.
     */
    public static String resolveFilterDate(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.isBlank()) {
            filterDate = request.getParameter("date");
        }
        return filterDate;
    }

    /** Chuẩn hóa key lọc ngày — ủy quyền Util. */
    public static String normalizeFilterKey(String filterDate) {
        return examstaff.util.AuditFilterHelper.normalizeFilterKey(filterDate);
    }
}
