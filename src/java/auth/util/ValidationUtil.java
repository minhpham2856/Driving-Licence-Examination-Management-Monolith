package auth.util;

import shared.enums.Sex;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ValidationUtil {

    // HTML <input type="date"> submits yyyy-MM-dd
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Unicode letters (incl. Vietnamese) + spaces; no digits or punctuation
    private static final String FULL_NAME_PATTERN = "^[\\p{L}]+(?:[\\s'\\-][\\p{L}]+)*$";

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

    // Họ tên: chỉ chữ cái (Unicode) và khoảng trắng / dấu nháy / gạch nối giữa các từ
    public static boolean isValidFullName(String value) {
        if (isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 200) {
            return false;
        }
        return trimmed.matches(FULL_NAME_PATTERN);
    }

    // Mật khẩu: >= 8, có chữ hoa, số, ký tự đặc biệt
    public static boolean isValidPassword(String value) {
        if (value == null || value.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    // Đủ 18 tuổi tính từ ngày tham chiếu (thường là hôm nay / ngày đăng ký)
    public static boolean isAdult(LocalDate dateOfBirth) {
        return isAdult(dateOfBirth, LocalDate.now());
    }

    public static boolean isAdult(LocalDate dateOfBirth, LocalDate asOf) {
        if (dateOfBirth == null || asOf == null) {
            return false;
        }
        return !dateOfBirth.plusYears(18).isAfter(asOf);
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
