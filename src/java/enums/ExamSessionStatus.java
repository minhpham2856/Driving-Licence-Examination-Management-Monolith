package enums;
public enum ExamSessionStatus {
    CHUA_DIEN_RA("Chưa diễn ra"),
    MO("Mở"),
    DANG_DIEN_RA("Đang diễn ra"),
    HOAN_TAT("Hoàn tất"),
    DA_HUY("Đã hủy");
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
        return CHUA_DIEN_RA;
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
