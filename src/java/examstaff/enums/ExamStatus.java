package examstaff.enums;

import java.util.Locale;

/** Trạng thái kỳ thi (Exam). */
public enum ExamStatus {
    CHUA_DIEN_RA("Chưa diễn ra"),
    MO("Mở"),
    DANG_DIEN_RA("Đang diễn ra"),
    HOAN_TAT("Hoàn tất"),
    DA_HUY("Đã hủy");

    private final String displayName;

    ExamStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

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
            case "completed", "complete" -> HOAN_TAT;
            case "cancelled", "canceled" -> DA_HUY;
            default -> CHUA_DIEN_RA;
        };
    }

    public static boolean canStart(String status) {
        ExamStatus normalized = normalize(status);
        return normalized == CHUA_DIEN_RA || normalized == MO;
    }

    public static boolean isInProgress(String status) {
        return normalize(status) == DANG_DIEN_RA;
    }
}
