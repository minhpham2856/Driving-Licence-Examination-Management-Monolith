package examstaff.enums;

/**
 * Enum loại đăng ký thí sinh trong kỳ: lần đầu ({@link #PRE_REGISTERED}) hoặc thi lại ({@link #RETAKE}).
 * Mã {@link #code} khớp giá trị lưu/so sánh trong CSDL và biểu thức SQL CASE.
 *
 * Vai trò trong luồng examstaff:
 * Phân biệt thí sinh đăng ký trước và thi lại trên dashboard, báo cáo và hàng đợi gọi.
 * {@link #sqlCaseExpression} sinh CASE SQL từ cột {@code takeNo}: null coi như 1 → PreRegistered;
 * {@code takeNo > 1} → Retake — nhúng trực tiếp vào SELECT JDBC.
 *
 * Giá trị mã kỹ thuật:
 * - {@link #PRE_REGISTERED} — {@code "PreRegistered"} (lần thi đầu).
 * - {@link #RETAKE} — {@code "Retake"} (thi lại).
 *
 * Ai sử dụng:
 * {@code ExamRegistrationDAOImpl}, {@code Db2CandidateSql}, {@code ExamStaffCandidateViewDAOImpl},
 * {@code ExamStaffCandidateMapper} — map sang {@code ExamRegistrationDTO#registrationType}.
 */
public enum RegistrationType {
    /** Đăng ký trước (lần thi đầu — takeNo ≤ 1 hoặc null). */
    PRE_REGISTERED("PreRegistered"),
    /** Thi lại (takeNo &gt; 1). */
    RETAKE("Retake");

    /** Mã kỹ thuật lưu trong chuỗi / SQL (không phải nhãn UI). */
    private final String code;

    /**
     * Gán mã kỹ thuật cho hằng enum.
     * @param code giá trị PreRegistered / Retake
     */
    RegistrationType(String code) {
        this.code = code;
    }

    /**
     * Biểu thức CASE SQL suy ra loại đăng ký từ cột số lần thi.
     * <p>
     * Khi {@code ISNULL(takeNoColumn, 1) > 1} → Retake; ngược lại → PreRegistered.
     * @param takeNoColumn tên cột số lần thi trong câu SQL (đã tin cậy, không user input thô)
     * @return đoạn CASE … END sẵn nhúng vào SELECT
     */
    public static String sqlCaseExpression(String takeNoColumn) {
        // takeNo null coi như 1 → PreRegistered; > 1 → Retake
        return "CASE WHEN ISNULL(" + takeNoColumn + ", 1) > 1 THEN N'"
                + RETAKE.code + "' ELSE N'" + PRE_REGISTERED.code + "' END";
    }
}
