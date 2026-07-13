package util;

public final class FormatUtil {

    private FormatUtil() {
    }

    public static String text(String str) {
        return (str == null || str.trim().isBlank()) ? null : str.trim();
    }

    public static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Integer toInteger(String str) {
        if (str == null || str.trim().isBlank()) {
            return null;
        }
        
        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
