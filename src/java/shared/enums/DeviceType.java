package enums;

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
        for (DeviceType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
