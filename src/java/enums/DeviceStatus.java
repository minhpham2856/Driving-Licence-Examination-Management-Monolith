package enums;

public enum DeviceStatus {
    HOAT_DONG("Hoạt động", "device-grid-card--available"),
    BAO_TRI("Bảo trì", "device-grid-card--maintenance"),
    SAN_SANG("Sẵn sàng", "device-grid-card--available");
    private final String displayName;
    private final String cssClass;

    DeviceStatus(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

    public static boolean isActive(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        DeviceStatus status = normalize(value);
        return status == HOAT_DONG || status == SAN_SANG;
    }

    public static DeviceStatus normalize(String value) {
        if (value == null || value.isBlank()) {
            return BAO_TRI;
        }
        String trimmed = value.trim();
        for (DeviceStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        return BAO_TRI;
    }

    public static String fromActive(boolean active) {
        return active ? HOAT_DONG.getDisplayName() : BAO_TRI.getDisplayName();
    }

    public static String readyLabel() {
        return SAN_SANG.getDisplayName();
    }

    public static String cssClassFor(boolean active) {
        return active ? HOAT_DONG.getCssClass() : BAO_TRI.getCssClass();
    }
}
