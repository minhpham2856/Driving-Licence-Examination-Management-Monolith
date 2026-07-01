package enums;
public enum RegistrationStatus {
    BAN_NHAP("Bản nháp"),
    CHO_DUYET("Chờ duyệt"),
    CAN_BO_SUNG("Cần bổ sung"),
    DUYET("Duyệt"),
    LOAI("Loại");
    private final String displayName;
    RegistrationStatus(String displayName) {
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
    public static RegistrationStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return BAN_NHAP;
        }
        String trimmed = value.trim();
        for (RegistrationStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return BAN_NHAP;
    }
    public static String normalizeDisplayName(String value) {
        return normalize(value).getDisplayName();
    }
    public static String badgeKey(String value) {
        return switch (normalize(value)) {
            case DUYET -> "success";
            case LOAI -> "danger";
            case CAN_BO_SUNG, CHO_DUYET -> "warning";
            default -> "info";
        };
    }
    public static boolean isReviewable(String value) {
        RegistrationStatus status = normalize(value);
        return status == BAN_NHAP || status == CHO_DUYET || status == CAN_BO_SUNG || status == LOAI;
    }
}
