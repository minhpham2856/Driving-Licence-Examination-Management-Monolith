package examstaff.enums;

import java.util.Locale;

/** Trạng thái kỳ thi (Exam). */
public enum ExamStatus {
    /** Kỳ thi chưa tới giờ. */
    CHUA_DIEN_RA("Chưa diễn ra"),
    /** Đã mở, chưa bắt đầu ca. */
    MO("Mở"),
    /** Đang diễn ra. */
    DANG_DIEN_RA("Đang diễn ra"),
    /** Tạm dừng ca. */
    TAM_DUNG("Tạm dừng"),
    /** Đã hoàn tất. */
    HOAN_TAT("Hoàn tất"),
    /** Đã hủy. */
    DA_HUY("Đã hủy");

    private final String displayName;

    ExamStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** So khớp chuỗi trạng thái (không phân biệt hoa thường). */
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

    /** Chuẩn hóa chuỗi trạng thái (VI/EN) về enum; mặc định chưa diễn ra. */
    public static ExamStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return CHUA_DIEN_RA;
        }
        String trimmed = value.trim();
        for (ExamStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "scheduled", "open" -> MO;
            case "inprogress", "in progress" -> DANG_DIEN_RA;
            case "paused", "pause", "tạm dừng", "tam dung" -> TAM_DUNG;
            case "completed", "complete" -> HOAN_TAT;
            case "cancelled", "canceled" -> DA_HUY;
            default -> CHUA_DIEN_RA;
        };
    }

    /** Có thể bắt đầu khi chưa diễn ra hoặc đang mở. */
    public static boolean canStart(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == CHUA_DIEN_RA || normalized == MO;
    }

    /** Đang diễn ra. */
    public static boolean isInProgress(String status) {
        return normalize(status) == DANG_DIEN_RA;
    }

    /** Đang tạm dừng. */
    public static boolean isPaused(String status) {
        return normalize(status) == TAM_DUNG;
    }

    /** Có thể kết thúc khi đang diễn ra hoặc đang tạm dừng. */
    public static boolean canEnd(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == DANG_DIEN_RA || normalized == TAM_DUNG;
    }

    /** Kỳ đã hoàn tất. */
    public static boolean isCompleted(String status) {
        return normalize(status) == HOAN_TAT;
    }

    /**
     * Khóa thao tác staff đổi hồ sơ / đình chỉ / hoàn tác khi kỳ đã đóng.
     * (Hoàn tất hoặc đã hủy.)
     */
    public static boolean isLockedForStaffMutation(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == HOAN_TAT || normalized == DA_HUY;
    }
}
