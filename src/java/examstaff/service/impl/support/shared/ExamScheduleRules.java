package examstaff.service.impl.support.shared;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Quy tắc thời gian kỳ thi (so sánh giờ bắt đầu/kết thúc) - helper thuần, không HTTP. */
public final class ExamScheduleRules {

    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter
            .ofPattern("HH:mm 'ngày' dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));

    private ExamScheduleRules() {
    }

    /**
     * Kiểm tra thời điểm hiện tại còn trước giờ bắt đầu lịch thi.
     *
     * @param scheduledStart giờ bắt đầu theo lịch (có thể null)
     * @return {@code true} nếu chưa tới giờ bắt đầu; {@code false} nếu null hoặc đã tới/qua
     */
    public static boolean isBeforeScheduledStart(Timestamp scheduledStart) {
        // Validate: thiếu lịch → không coi là “trước giờ”
        if (scheduledStart == null) {
            return false;
        }
        // Result: so sánh Instant hiện tại với mốc lịch
        return Instant.now().isBefore(scheduledStart.toInstant());
    }

    /**
     * Kỳ thi có thể bắt đầu ngay (đã tới hoặc qua giờ lịch).
     *
     * @param scheduledStart giờ bắt đầu theo lịch
     * @return {@code true} nếu được phép bắt đầu
     */
    public static boolean canStartNow(Timestamp scheduledStart) {
        return !isBeforeScheduledStart(scheduledStart);
    }

    /**
     * Định dạng giờ bắt đầu lịch thi theo múi giờ Việt Nam (HH:mm ngày dd/MM/yyyy).
     *
     * @param scheduledStart giờ bắt đầu
     * @return chuỗi hiển thị, hoặc rỗng nếu null
     */
    public static String formatScheduledStart(Timestamp scheduledStart) {
        // Validate
        if (scheduledStart == null) {
            return "";
        }
        // Result: format theo Asia/Ho_Chi_Minh
        return scheduledStart.toInstant().atZone(VIETNAM).format(DISPLAY);
    }
}
