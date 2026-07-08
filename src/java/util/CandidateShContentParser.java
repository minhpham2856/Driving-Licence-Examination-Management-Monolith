package util;

import dto.exam.ExamRegistrationDTO;

import java.util.Locale;

/**
 * Phân tích cột «Nội dung SH» (DSTS / PC08).
 *
 * Quy ước mã: L = Lý thuyết, H = Thực hành (sa hình), Đ = Đường trường,
 * nối bằng dấu «+» (ví dụ: "L", "L+H", "L+H+Đ", "H", "Đ", "H+Đ").
 *
 * NULL = có thi phần đó; FALSE = bảo lưu, không thi phần đó.
 * Nếu không nhận diện được phần nào (văn bản mơ hồ) thì giữ mặc định NULL
 * cho cả ba (an toàn: coi như thi đầy đủ).
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

        boolean hasTheory = hasTheory(lower);
        boolean hasPractical = hasPractical(lower);
        boolean hasRoad = hasRoad(lower);

        // Chỉ suy ra cờ khi nhận diện được ít nhất một phần thi rõ ràng.
        // Phần có mặt -> null (thi); phần vắng -> FALSE (bảo lưu).
        if (hasTheory || hasPractical || hasRoad) {
            reg.setTakeTheory(hasTheory ? null : Boolean.FALSE);
            reg.setTakePractical(hasPractical ? null : Boolean.FALSE);
            reg.setTakeOnRoad(hasRoad ? null : Boolean.FALSE);
        }
    }

    private static boolean hasTheory(String lower) {
        return hasToken(lower, "l")
                || lower.contains("lý thuyết") || lower.contains("ly thuyet")
                || lower.contains("theory");
    }

    private static boolean hasPractical(String lower) {
        return hasToken(lower, "h")
                || lower.contains("sa hình") || lower.contains("sa hinh")
                || lower.contains("thực hành") || lower.contains("thuc hanh")
                || lower.contains("practical");
    }

    private static boolean hasRoad(String lower) {
        return hasToken(lower, "đ", "d")
                || lower.contains("đường") || lower.contains("duong")
                || lower.contains("road");
    }

    /**
     * Trả về true nếu một trong các mã xuất hiện như một token độc lập
     * (được ngăn cách bởi ký tự không phải chữ cái: khoảng trắng, «+», «,»...).
     * Nhờ vậy "SH" không bị hiểu nhầm thành mã "H", "lại" không thành "L".
     */
    private static boolean hasToken(String lower, String... codes) {
        for (String token : lower.split("[^\\p{L}]+")) {
            for (String code : codes) {
                if (token.equals(code)) {
                    return true;
                }
            }
        }
        return false;
    }
}
