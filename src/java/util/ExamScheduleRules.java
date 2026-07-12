package util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Quy tắc thời gian kỳ thi (so sánh giờ bắt đầu/kết thúc) — helper thuần, không HTTP. */
public final class ExamScheduleRules {

    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter
            .ofPattern("HH:mm 'ngày' dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));

    private ExamScheduleRules() {
    }

    public static boolean isBeforeScheduledStart(Timestamp scheduledStart) {
        if (scheduledStart == null) {
            return false;
        }
        return Instant.now().isBefore(scheduledStart.toInstant());
    }

    public static boolean canStartNow(Timestamp scheduledStart) {
        return !isBeforeScheduledStart(scheduledStart);
    }

    public static String formatScheduledStart(Timestamp scheduledStart) {
        if (scheduledStart == null) {
            return "";
        }
        return scheduledStart.toInstant().atZone(VIETNAM).format(DISPLAY);
    }
}
