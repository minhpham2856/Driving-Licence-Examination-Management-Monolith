package enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum DeviceType {
    COMPUTER("Computer", "Máy thi", "computer", true),
    MOTORCYCLE("Motorcycle", "Xe máy", "two_wheeler", false),
    CAR("Car", "Ô tô", "directions_car", false),
    TRUCK("Truck", "Xe tải", "local_shipping", false);

    private final String typeName;
    private final String labelVi;
    private final String icon;
    private final boolean isComputer;

    DeviceType(String typeName, String labelVi, String icon, boolean isComputer) {
        this.typeName = typeName;
        this.labelVi = labelVi;
        this.icon = icon;
        this.isComputer = isComputer;
    }

    public String getTypeName() {
        return typeName;
    }

    public static boolean isComputer(String deviceType) {
        return deviceType != null && COMPUTER.typeName.equalsIgnoreCase(deviceType.trim());
    }

    public static boolean isVehicle(String deviceType) {
        if (deviceType == null) return false;
        String normalized = deviceType.trim();
        return MOTORCYCLE.typeName.equalsIgnoreCase(normalized) ||
               CAR.typeName.equalsIgnoreCase(normalized) ||
               TRUCK.typeName.equalsIgnoreCase(normalized);
    }

    public static List<String> vehicleTypesForLicence(String licenceClass) {
        String lc = licenceClass != null ? licenceClass.trim().toUpperCase(Locale.ROOT) : "";
        List<String> types = new ArrayList<>();
        if ("A1".equals(lc) || "A".equals(lc)) {
            types.add(MOTORCYCLE.typeName);
            return types;
        }
        if ("C".equals(lc) || lc.startsWith("D") || "FC".equals(lc)) {
            types.add(CAR.typeName);
            types.add(TRUCK.typeName);
            return types;
        }
        types.add(CAR.typeName);
        return types;
    }

    public static boolean matchesLicence(String licenceClass, String deviceType) {
        if (deviceType == null) return false;
        String normalized = deviceType.trim();
        for (String allowed : vehicleTypesForLicence(licenceClass)) {
            if (allowed.equalsIgnoreCase(normalized)) return true;
        }
        return false;
    }

    public static String iconFor(String deviceType) {
        if (deviceType == null) return "devices";
        for (DeviceType dt : values()) {
            if (dt.typeName.equalsIgnoreCase(deviceType.trim())) {
                return dt.icon;
            }
        }
        return "devices";
    }

    public static String typeLabelVi(String deviceType) {
        if (deviceType == null) return "Thiết bị";
        for (DeviceType dt : values()) {
            if (dt.typeName.equalsIgnoreCase(deviceType.trim())) {
                return dt.labelVi;
            }
        }
        return deviceType;
    }
}
