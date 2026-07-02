package enums;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public enum DeviceType {
    MAY_TINH("Máy tính", "computer"),
    MO_TO("Mô tô", "two_wheeler"),
    XE_CON("Xe con", "directions_car"),
    XE_TAI("Xe tải", "local_shipping");
    private final String displayName;
    private final String icon;
    DeviceType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getIcon() {
        return icon;
    }
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }
    public static DeviceType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (DeviceType type : values()) {
            if (type.matches(value)) {
                return type;
            }
        }
        return null;
    }
    public static boolean isComputer(String value) {
        return fromValue(value) == MAY_TINH;
    }
    public static boolean isVehicle(String value) {
        DeviceType type = fromValue(value);
        return type == MO_TO || type == XE_CON || type == XE_TAI;
    }
    public static List<String> typesForLicenceClass(String licenceClass) {
        String lc = licenceClass != null ? licenceClass.trim().toUpperCase(Locale.ROOT) : "";
        List<String> types = new ArrayList<>();
        if ("A1".equals(lc) || "A".equals(lc)) {
            types.add(MO_TO.getDisplayName());
            return types;
        }
        if ("C".equals(lc) || lc.startsWith("D") || "FC".equals(lc)) {
            types.add(XE_CON.getDisplayName());
            types.add(XE_TAI.getDisplayName());
            return types;
        }
        types.add(XE_CON.getDisplayName());
        return types;
    }
    public static boolean matchesLicenceClass(String licenceClass, String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            return false;
        }
        for (String allowed : typesForLicenceClass(licenceClass)) {
            if (allowed.equalsIgnoreCase(deviceType.trim())) {
                return true;
            }
        }
        return false;
    }
    public static String iconFor(String value) {
        DeviceType type = fromValue(value);
        return type != null ? type.getIcon() : "devices";
    }
}
