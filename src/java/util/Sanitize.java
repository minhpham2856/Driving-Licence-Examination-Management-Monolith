package util;


/**
 * Minimal helpers for reading & trimming request parameters.
 */
public final class Sanitize {

    private Sanitize() {
    }

    public static String text(String v) {
        return v == null ? "" : v.trim();
    }

    public static int toInt(String v, int def) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    public static Integer toIntegerOrNull(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
