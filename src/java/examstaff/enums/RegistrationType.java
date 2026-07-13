package examstaff.enums;

public enum RegistrationType {
    PRE_REGISTERED("PreRegistered"),
    RETAKE("Retake");

    private final String code;

    RegistrationType(String code) {
        this.code = code;
    }

    public static String sqlCaseExpression(String takeNoColumn) {
        return "CASE WHEN ISNULL(" + takeNoColumn + ", 1) > 1 THEN N'"
                + RETAKE.code + "' ELSE N'" + PRE_REGISTERED.code + "' END";
    }
}
