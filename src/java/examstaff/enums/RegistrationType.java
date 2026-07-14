package examstaff.enums;

/** Loại đăng ký thí sinh (đăng ký trước / thi lại). */
public enum RegistrationType {
    /** Đăng ký trước (lần thi đầu). */
    PRE_REGISTERED("PreRegistered"),
    /** Thi lại (takeNo > 1). */
    RETAKE("Retake");

    private final String code;

    RegistrationType(String code) {
        this.code = code;
    }

    /** Biểu thức CASE SQL suy ra loại đăng ký từ cột số lần thi. */
    public static String sqlCaseExpression(String takeNoColumn) {
        return "CASE WHEN ISNULL(" + takeNoColumn + ", 1) > 1 THEN N'"
                + RETAKE.code + "' ELSE N'" + PRE_REGISTERED.code + "' END";
    }
}
