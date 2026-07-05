package util;

import enums.Sex;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CredentialsUtil {

    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final DateTimeFormatter VIETNAMESE_DATE = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private CredentialsUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidCccd(String value) {
        return value != null && CCCD_PATTERN.matcher(value).matches();
    }

    public static boolean isValidPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }

    public static Optional<LocalDate> parseIsoDate(String value) {
        if (isBlank(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), ISO_DATE));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDate> parseVietnameseDate(String value) {
        if (isBlank(value)) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        try {
            if (trimmed.contains("/")) {
                return Optional.of(LocalDate.parse(trimmed, VIETNAMESE_DATE));
            }
            return parseIsoDate(trimmed);
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDate> parseDate(String value) {
        Optional<LocalDate> vietnamese = parseVietnameseDate(value);
        if (vietnamese.isPresent()) {
            return vietnamese;
        }
        return parseIsoDate(value);
    }

    public static boolean isValidSex(String value) {
        return Sex.fromValue(value) != null;
    }

    public static boolean isValidManagedUserType(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return "student".equals(normalized) || "free".equals(normalized);
    }

    public static boolean isLengthInRange(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    public static String normalizeLicenceClass(String value) {
        if (value == null) {
            return "";
        }
        String licenseClass = value.trim().toUpperCase();
        if ("A".equals(licenseClass)) {
            return "A2";
        }
        if ("B".equals(licenseClass)) {
            return "B2";
        }
        return licenseClass;
    }
}
