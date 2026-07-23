package shared.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Quy tắc đóng đăng ký ngày thi dự kiến trước 07 ngày làm việc. */
public final class TentativeExamDatePolicy {

    public static final int LOCK_BEFORE_WORKING_DAYS = 7;

    private TentativeExamDatePolicy() {
    }

    /** Ngày hệ thống bắt đầu khóa (tính lùi, bỏ thứ Bảy và Chủ nhật). */
    public static LocalDate lockDate(LocalDate examDate) {
        if (examDate == null) {
            return null;
        }
        LocalDate cursor = examDate;
        int counted = 0;
        while (counted < LOCK_BEFORE_WORKING_DAYS) {
            cursor = cursor.minusDays(1);
            if (isWorkingDay(cursor)) {
                counted++;
            }
        }
        return cursor;
    }

    /** Từ đúng ngày khóa trở đi thì không còn được đăng ký/hủy. */
    public static boolean shouldBeLocked(LocalDate examDate, LocalDate today) {
        LocalDate deadline = lockDate(examDate);
        return deadline != null && today != null && !today.isBefore(deadline);
    }

    /** Ngày gần nhất có thể tạo mà không bị khóa ngay trong ngày tạo. */
    public static LocalDate earliestCreatableDate(LocalDate today) {
        LocalDate cursor = today;
        int counted = 0;
        while (counted < LOCK_BEFORE_WORKING_DAYS + 1) {
            cursor = cursor.plusDays(1);
            if (isWorkingDay(cursor)) {
                counted++;
            }
        }
        return cursor;
    }

    public static boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
