package util;

import enums.DeviceType;
import enums.DeviceStatus;

import java.util.Map;

public final class FormatUtil {

    private FormatUtil() {
    }

    public static int parseCandidateNo(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) return 0;
        String trimmed = candidateNumber.trim();
        if (trimmed.contains("-")) {
            try {
                return Integer.parseInt(trimmed.substring(trimmed.indexOf('-') + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String buildCandidateNumber(String licenseCode, int candidateNo) {
        if (candidateNo <= 0) return "000";
        return candidateNo < 1000
                ? String.format("%03d", candidateNo)
                : String.valueOf(candidateNo);
    }

    public static String formatSbd(int candidateNo) {
        return buildCandidateNumber(null, candidateNo);
    }

    public static void enrichDeviceRow(Map<String, Object> row, String licenceClass) {
        if (row == null) return;
        String type = row.get("type") != null ? String.valueOf(row.get("type")) : "";
        row.put("icon", DeviceType.iconFor(type));
        row.put("typeLabel", DeviceType.typeLabelVi(type));
        row.put("statusLabel", DeviceStatus.statusLabelVi(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("statusClass", DeviceStatus.statusCssClass(row.get("status") != null ? String.valueOf(row.get("status")) : null));
        row.put("vehicle", DeviceType.isVehicle(type));
        row.put("computer", DeviceType.isComputer(type));
        row.put("licenceMatch", DeviceType.isComputer(type) || DeviceType.matchesLicence(licenceClass, type));
    }
}
