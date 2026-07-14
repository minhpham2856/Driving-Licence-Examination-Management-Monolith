package examstaff.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Quy tắc thời gian kỳ thi (so sánh giờ bắt đầu/kết thúc) — helper thuần. */
public final class ExamScheduleRules {

    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter
            .ofPattern("HH:mm 'ngày' dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));

    private ExamScheduleRules() {
    }

    /**
     * Hiện tại còn trước giờ bắt đầu dự kiến.
     *
     * @param scheduledStart mốc bắt đầu (null → false)
     * @return {@code true} nếu chưa tới giờ
     */
    public static boolean isBeforeScheduledStart(Timestamp scheduledStart) {
        if (scheduledStart == null) {
            return false;
        }
        return Instant.now().isBefore(scheduledStart.toInstant());
    }

    /**
     * Có thể bắt đầu kỳ ngay (đã tới hoặc qua giờ dự kiến).
     *
     * @param scheduledStart mốc bắt đầu
     * @return {@code true} nếu được phép start
     */
    public static boolean canStartNow(Timestamp scheduledStart) {
        return !isBeforeScheduledStart(scheduledStart);
    }

    /**
     * Format giờ bắt đầu theo múi giờ Việt Nam.
     *
     * @param scheduledStart mốc bắt đầu
     * @return chuỗi {@code HH:mm ngày dd/MM/yyyy} hoặc {@code ""}
     */
    public static String formatScheduledStart(Timestamp scheduledStart) {
        if (scheduledStart == null) {
            return "";
        }
        return scheduledStart.toInstant().atZone(VIETNAM).format(DISPLAY);
    }
}
