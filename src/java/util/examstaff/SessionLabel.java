package util.examstaff;

/**
 * Nhãn ca thi từ IsMorningSession + tên phần thi (thay cột SessionName đã bỏ).
 */
public final class SessionLabel {

    private SessionLabel() {
    }

    public static String shiftLabel(boolean morning) {
        return morning ? "Ca sáng" : "Ca chiều";
    }

    public static String shiftLabel(Boolean morning) {
        if (morning == null) {
            return "Ca thi";
        }
        return shiftLabel(morning.booleanValue());
    }

    /** Ví dụ: "Ca sáng - Lý thuyết" */
    public static String of(boolean morning, String sectionName) {
        String shift = shiftLabel(morning);
        if (sectionName == null || sectionName.isBlank()) {
            return shift;
        }
        return shift + " - " + sectionName.trim();
    }

    public static String of(Boolean morning, String sectionName) {
        return of(morning != null && morning, sectionName);
    }

    /** SQL expression (alias s = Session) — không kèm phần thi. */
    public static final String SQL_SHIFT_ONLY =
            "(CASE WHEN s.IsMorningSession = 1 THEN N'Ca sáng' ELSE N'Ca chiều' END)";

    /**
     * SQL expression kèm phần thi (cần alias sect.examTypeName hoặc tương đương).
     */
    public static final String SQL_WITH_SECTION =
            SQL_SHIFT_ONLY
                    + " + CASE WHEN sect.examTypeName IS NULL OR LTRIM(RTRIM(sect.examTypeName)) = N''"
                    + " THEN N'' ELSE N' - ' + sect.examTypeName END";
}
