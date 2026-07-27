package shared.util;

public final class FormatUtil {

    public static String formatString(String str) {
        return (str == null || str.trim().isBlank()) ? null : str.trim();
    }

    public static int formatInt(String str, int def) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Integer formatInteger(String str) {
        if (str == null || str.trim().isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Returns parsed int when raw is a positive number; otherwise 0.
    public static int formatPositiveInt(String raw) {
        Integer value = formatPositiveInteger(raw);
        if (value == null) {
            return 0;
        }
        return value;
    }

    // Returns parsed Integer when raw is a positive number; otherwise null.
    public static Integer formatPositiveInteger(String raw) {
        if (raw == null || raw.trim().isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
