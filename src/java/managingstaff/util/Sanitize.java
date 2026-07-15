package managingstaff.util;

public final class Sanitize {
    private Sanitize() { }
    public static String text(String value) { return value == null ? "" : value.trim(); }
    public static int toInt(String value, int fallback) {
        try { return Integer.parseInt(text(value)); }
        catch (NumberFormatException ex) { return fallback; }
    }
}
