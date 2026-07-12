package enums;

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

    public static DeviceStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DeviceStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
