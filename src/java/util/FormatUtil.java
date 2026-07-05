package util;

public class FormatUtil {

    public static String format(String str) {
        return (str == null || str.trim().isBlank()) ? null : str.trim();
    }
}
