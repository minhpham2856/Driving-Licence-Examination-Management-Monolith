package auth.util;

import shared.enums.Sex;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ValidationUtil {

    // HTML <input type="date"> submits yyyy-MM-dd
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidCccd(String value) {
        return value != null && value.matches("\\d{12}");
    }

    public static boolean isValidPhone(String value) {
        return value != null && value.matches("0\\d{9}");
    }

    public static boolean isValidEmail(String value) {
        return value != null && value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    public static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDate.parse(trimmed, DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(trimmed, LEGACY_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static boolean isValidSex(String value) {
        return Sex.fromValue(value) != null;
    }
}
