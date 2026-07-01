package util;
public final class Sanitize {
    private Sanitize() {
    }
    public static String text(String str) {
        return str == null ? "" : str.trim();
    }
    public static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return def;
        }
    }
    public static Integer toIntegerOrNull(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(str.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
