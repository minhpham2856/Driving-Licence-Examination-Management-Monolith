package util;

import dto.exam.ExamRegistrationDTO;

import java.util.Locale;

/**
 * Phân tích cột «Nội dung SH» (DSTS / PC08).
 * NULL = thi phần đó; FALSE = bảo lưu, không thi lại.
 */
public final class CandidateShContentParser {

    private CandidateShContentParser() {
    }

    public static void apply(String shContent, ExamRegistrationDTO reg) {
        String raw = shContent == null ? "" : shContent.trim();
        reg.setReasonForTaking(raw);

        reg.setTakeTheory(null);
        reg.setTakePractical(null);
        reg.setTakeOnRoad(null);
        reg.setTakeNo(1);

        if (raw.isEmpty()) {
            return;
        }

        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("lại") || lower.contains("lai")) {
            reg.setTakeNo(2);
        } else if (lower.contains("lần đầu") || lower.contains("lan dau")) {
            reg.setTakeNo(1);
        }

        if (isTheoryOnlyRetake(lower)) {
            reg.setTakeTheory(null);
            reg.setTakePractical(Boolean.FALSE);
            reg.setTakeOnRoad(Boolean.FALSE);
            return;
        }

        if (isPracticalOnly(lower)) {
            reg.setTakeTheory(Boolean.FALSE);
            reg.setTakePractical(null);
            reg.setTakeOnRoad(Boolean.FALSE);
            return;
        }

        if (isRoadOnly(lower)) {
            reg.setTakeTheory(Boolean.FALSE);
            reg.setTakePractical(Boolean.FALSE);
            reg.setTakeOnRoad(null);
            return;
        }

        // SH lần đầu / thi lại cả L+H (+ Đ nếu có)
        if (containsTheory(lower)) {
            reg.setTakeTheory(null);
        }
        if (containsPractical(lower)) {
            reg.setTakePractical(null);
        }
        if (containsRoad(lower)) {
            reg.setTakeOnRoad(null);
        }
    }

    private static boolean isTheoryOnlyRetake(String lower) {
        return (lower.contains("sh lại l") || lower.contains("sh lai l") || lower.contains("thi lại l"))
                && !lower.contains("l+h") && !lower.contains("l + h");
    }

    private static boolean isPracticalOnly(String lower) {
        return lower.contains("sát hạch h") || lower.contains("sat hach h")
                || lower.contains("sh lại h") || lower.contains("sh lai h");
    }

    private static boolean isRoadOnly(String lower) {
        return (lower.contains("sh lại đ") || lower.contains("sh lai d") || lower.contains("sát hạch đ"))
                && !lower.contains("l+h");
    }

    private static boolean containsTheory(String lower) {
        return lower.contains("l+h") || lower.contains("l + h") || lower.contains("+l")
                || lower.matches(".*\\bl\\b.*") && !isPracticalOnly(lower);
    }

    private static boolean containsPractical(String lower) {
        return lower.contains("l+h") || lower.contains("h") || lower.contains("sa hình")
                || lower.contains("sát hạch");
    }

    private static boolean containsRoad(String lower) {
        return lower.contains("đường") || lower.contains("duong")
                || lower.contains("+đ") || lower.contains("+d")
                || lower.matches(".*\\bđ\\b.*");
    }
}
