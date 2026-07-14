package auth.util;

import shared.enums.Sex;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ValidationUtil {

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
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static boolean isValidSex(String value) {
        return Sex.fromValue(value) != null;
    }
}
