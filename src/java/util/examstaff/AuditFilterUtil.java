package util.examstaff;

import jakarta.servlet.http.HttpServletRequest;

public final class AuditFilterUtil {

    private AuditFilterUtil() {
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
