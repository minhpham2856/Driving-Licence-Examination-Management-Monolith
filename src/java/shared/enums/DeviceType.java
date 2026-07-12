package shared.enums;

public enum DeviceType {
    COMPUTER("Máy tính"),
    MOTORCYCLE("Mô tô"),
    CAR("Xe con"),
    TRUCK("Xe tải");

    private final String value;

    private DeviceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeviceType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DeviceType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
