package Utils;

import Enums.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceUtils {

    public static boolean isComputer(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return false;
        }
        return DeviceType.COMPUTER.getValue().equalsIgnoreCase(deviceType.trim());
    }

    public static boolean isVehicle(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return false;
        }
        String t = deviceType.trim();
        return DeviceType.MOTORCYCLE.getValue().equalsIgnoreCase(t)
                || DeviceType.CAR.getValue().equalsIgnoreCase(t)
                || DeviceType.TRUCK.getValue().equalsIgnoreCase(t);
    }

    public static List<String> vehicleTypesForLicence(String licenceClass) {
        List<String> types = new ArrayList<>();
        if (licenceClass == null || licenceClass.trim().isEmpty()) {
            return types;
        }
        String lClass = licenceClass.trim().toUpperCase();
        if (lClass.startsWith("A")) {
            types.add(DeviceType.MOTORCYCLE.getValue());
            return types;
        }
        if ("C".equals(lClass) || "D".equals(lClass) || "E".equals(lClass) || "F".equals(lClass) || "FC".equals(lClass) || "FD".equals(lClass) || "FE".equals(lClass)) {
            types.add(DeviceType.TRUCK.getValue());
            return types;
        }
        types.add(DeviceType.CAR.getValue());
        return types;
    }

    public static boolean matchesLicence(String licenceClass, String deviceType) {
        if (deviceType == null) {
            return false;
        }
        String normalized = deviceType.trim();
        for (String allowed : vehicleTypesForLicence(licenceClass)) {
            if (allowed.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static String iconFor(String deviceType) {
        if (deviceType == null) {
            return "devices";
        }
        String type = deviceType.trim().toLowerCase(Locale.ROOT);
        if ("computer".equals(type)) return "computer";
        if ("motorcycle".equals(type)) return "two_wheeler";
        if ("car".equals(type)) return "directions_car";
        if ("truck".equals(type)) return "local_shipping";
        return "devices";
    }

    public static String typeLabelVi(String deviceType) {
        if (deviceType == null) {
            return "Thiết bị";
        }
        String type = deviceType.trim().toLowerCase(Locale.ROOT);
        if ("computer".equals(type)) return "Máy thi";
        if ("motorcycle".equals(type)) return "Xe máy";
        if ("car".equals(type)) return "Ô tô";
        if ("truck".equals(type)) return "Xe tải";
        return deviceType;
    }

    public static String statusLabelVi(String status) {
        if (status == null) {
            return "-";
        }
        String s = status.trim();
        if ("Available".equalsIgnoreCase(s) || "Operational".equalsIgnoreCase(s)) return "Sẵn sàng";
        if ("InUse".equalsIgnoreCase(s)) return "Đang dùng";
        if ("Maintenance".equalsIgnoreCase(s)) return "Bảo trì";
        return status;
    }

    public static String statusCssClass(String status) {
        if (status == null) {
            return "device-grid-card--unknown";
        }
        String s = status.trim();
        if ("Available".equalsIgnoreCase(s) || "Operational".equalsIgnoreCase(s)) return "device-grid-card--available";
        if ("InUse".equalsIgnoreCase(s)) return "device-grid-card--inuse";
        if ("Maintenance".equalsIgnoreCase(s)) return "device-grid-card--maintenance";
        return "device-grid-card--unknown";
    }

    public static void enrichDeviceRow(Map<String, Object> row, String licenceClass) {
        if (row == null) {
            return;
        }
        String type = row.get("type") != null ? String.valueOf(row.get("type")) : "";
        row.put("icon", iconFor(type));
        row.put("typeLabel", typeLabelVi(type));
        row.put("statusLabel", statusLabelVi(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("statusClass", statusCssClass(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("vehicle", isVehicle(type));
        row.put("computer", isComputer(type));
        row.put("licenceMatch", isComputer(type) || matchesLicence(licenceClass, type));
    }
}
