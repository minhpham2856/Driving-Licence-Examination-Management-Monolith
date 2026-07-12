package shared.enums;

import java.util.Locale;

public enum ExamSessionStatus {
    CHUA_DIEN_RA("ChÆ°a diá»…n ra"),
    MO("Má»Ÿ"),
    DANG_DIEN_RA("Äang diá»…n ra"),
    HOAN_TAT("HoÃ n táº¥t"),
    DA_HUY("ÄÃ£ há»§y");
    private final String displayName;
    ExamSessionStatus(String displayName) {
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
    public static ExamSessionStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return CHUA_DIEN_RA;
        }
        String trimmed = value.trim();
        for (ExamSessionStatus status : values()) {
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
        ExamSessionStatus normalized = normalize(status);
        return normalized == CHUA_DIEN_RA || normalized == MO;
    }
    public static boolean isInProgress(String status) {
        return normalize(status) == DANG_DIEN_RA;
    }
    public static boolean isEnded(String status) {
        ExamSessionStatus normalized = normalize(status);
        return normalized == HOAN_TAT || normalized == DA_HUY;
    }
}

