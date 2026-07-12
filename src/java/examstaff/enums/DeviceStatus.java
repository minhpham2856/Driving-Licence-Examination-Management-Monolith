package examstaff.enums;

public enum DeviceStatus {
    ACTIVE("Hoạt động"),
    MAINTENANCE("Bảo trì");

    private final String value;

    private DeviceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return value;
    }

    public static DeviceStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DeviceStatus status : values()) {
            if (status.getValue().equals(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
    
    public static DeviceStatus normalize(String value) {
        DeviceStatus status = fromValue(value);
        return status != null ? status : ACTIVE;
    }
    
    public String getCssClass() {
        return this == ACTIVE ? "text-success" : "text-danger";
    }
}
