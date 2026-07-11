package examstaff.enums;

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
    
    public String getDisplayName() {
        return value;
    }

    public static DeviceType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DeviceType type : values()) {
            if (type.getValue().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
    
    public static String iconFor(String type) {
        if (type == null) return "";
        if (type.equalsIgnoreCase("Máy tính") || type.equalsIgnoreCase("COMPUTER")) return "fa-desktop";
        if (type.equalsIgnoreCase("Mô tô") || type.equalsIgnoreCase("MOTORCYCLE")) return "fa-motorcycle";
        return "fa-car";
    }
    
    public static boolean isVehicle(String type) {
        return type != null && (type.equalsIgnoreCase("Mô tô") || type.equalsIgnoreCase("Xe con") || type.equalsIgnoreCase("Xe tải") || type.equalsIgnoreCase("MOTORCYCLE") || type.equalsIgnoreCase("CAR") || type.equalsIgnoreCase("TRUCK"));
    }
    
    public static boolean isComputer(String type) {
        return type != null && (type.equalsIgnoreCase("Máy tính") || type.equalsIgnoreCase("COMPUTER"));
    }
    
    public static boolean matchesLicenceClass(String licenceClass, String type) {
        return true; // dummy implementation to fix compile
    }
}
