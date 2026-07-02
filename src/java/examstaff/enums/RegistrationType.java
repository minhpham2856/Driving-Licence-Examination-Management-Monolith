package examstaff.enums;

import java.util.Locale;

public enum RegistrationType {
    PRE_REGISTERED("PreRegistered"),
    RETAKE("Retake");

    private final String code;

    RegistrationType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static RegistrationType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return PRE_REGISTERED;
        }
        String trimmed = value.trim();
        for (RegistrationType type : values()) {
            if (type.code.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        return PRE_REGISTERED;
    }

    public static String sqlCaseExpression(String takeNoColumn) {
        return "CASE WHEN ISNULL(" + takeNoColumn + ", 1) > 1 THEN N'"
                + RETAKE.code + "' ELSE N'" + PRE_REGISTERED.code + "' END";
    }
}
