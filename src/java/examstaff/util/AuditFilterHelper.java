package examstaff.util;

public final class AuditFilterHelper {

    private AuditFilterHelper() {
    }

    public static String normalizeFilterKey(String filterDate) {
        return filterDate == null ? "" : filterDate.trim();
    }
}
