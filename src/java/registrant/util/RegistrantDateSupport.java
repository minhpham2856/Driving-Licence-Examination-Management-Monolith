package registrant.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Parse và hiển thị ngày theo định dạng quen thuộc dd/MM/yyyy.
 */
public final class RegistrantDateSupport {

    public static final String DISPLAY_PATTERN = "dd/MM/yyyy";

    private static final DateTimeFormatter VI_DATE = DateTimeFormatter
            .ofPattern(DISPLAY_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
            .withLocale(Locale.forLanguageTag("vi-VN"));

    private RegistrantDateSupport() {
    }

    /** Trim tham số ngày (ủy quyền RegistrantListFilter). */
    public static String trimParam(String raw) {
        return RegistrantListFilter.trimParam(raw);
    }

    /** Format LocalDate thành dd/MM/yyyy. */
    public static String format(LocalDate date) {
        return date == null ? "" : date.format(VI_DATE);
    }

    /** Giá trị cho {@code <input type="date">} (yyyy-MM-dd). */
    public static String toIsoValue(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    /**
     * Parse ngày từ form. Hỗ trợ dd/MM/yyyy và yyyy-MM-dd (bookmark cũ).
     */
    public static LocalDate parse(String raw) {
        String trimmed = trimParam(raw);
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(trimmed);
        }
        return LocalDate.parse(trimmed, VI_DATE);
    }

    /** Validate một trường ngày; trả message lỗi hoặc null. */
    public static String validateDateField(String label, String raw) {
        String trimmed = trimParam(raw);
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            parse(trimmed);
            return null;
        } catch (DateTimeParseException ex) {
            return label + " không hợp lệ. Vui lòng nhập theo định dạng " + DISPLAY_PATTERN + ".";
        }
    }

    /** Validate from/to và đảm bảo from ≤ to. */
    public static String validateDateRange(String fromRaw, String toRaw) {
        String fromError = validateDateField("Từ ngày", fromRaw);
        if (fromError != null) {
            return fromError;
        }
        String toError = validateDateField("Đến ngày", toRaw);
        if (toError != null) {
            return toError;
        }

        LocalDate from = parse(fromRaw);
        LocalDate to = parse(toRaw);
        if (from != null && to != null && from.isAfter(to)) {
            return "Từ ngày không được sau Đến ngày.";
        }
        return null;
    }

    /** Chuẩn hóa chuỗi ngày về dạng hiển thị dd/MM/yyyy. */
    public static String displayValue(String raw) {
        String trimmed = trimParam(raw);
        if (trimmed.isEmpty()) {
            return "";
        }
        try {
            return format(parse(trimmed));
        } catch (DateTimeParseException ex) {
            return trimmed;
        }
    }
}
