package examstaff.util;

import jakarta.servlet.http.HttpServletRequest;

public final class AuditFilterSupport {

    private AuditFilterSupport() {
    }

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

    public static String normalizeFilterKey(String filterDate) {
        return filterDate == null ? "" : filterDate.trim();
    }
}
