package enums;
public enum SectionStatus {
    CHUA_THI("Chưa thi"),
    DANG_THI("Đang thi"),
    CHO_KY("Chờ ký"),
    DA_THI("Đã thi");
    private final String displayName;
    SectionStatus(String displayName) {
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
    public static SectionStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return CHUA_THI;
        }
        String trimmed = value.trim();
        for (SectionStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return CHUA_THI;
    }
    public static String normalizeDisplayName(String value) {
        return normalize(value).getDisplayName();
    }
    public static boolean isAwaitingSignature(String value) {
        return normalize(value) == CHO_KY;
    }
    public static boolean isDone(String value) {
        return normalize(value) == DA_THI;
    }
    public static boolean isEligibleForScoreQueue(String value) {
        SectionStatus status = normalize(value);
        return status == DANG_THI || status == CHUA_THI;
    }
    public static String statusKey(String value) {
        return switch (normalize(value)) {
            case DA_THI -> "done";
            case CHO_KY -> "awaiting";
            case DANG_THI -> "testing";
            case CHUA_THI -> "pending";
        };
    }
}
