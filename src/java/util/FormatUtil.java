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
        DeviceType typeEnum = DeviceType.fromValue(type);
        row.put("typeLabel", typeEnum != null ? typeEnum.getDisplayName() : type);
        DeviceStatus statusEnum = DeviceStatus.normalize(row.get("status") != null ? String.valueOf(row.get("status")) : null);
        row.put("statusLabel", statusEnum.getDisplayName());
        row.put("statusClass", statusEnum.getCssClass());
        row.put("vehicle", DeviceType.isVehicle(type));
        row.put("computer", DeviceType.isComputer(type));
        row.put("licenceMatch", DeviceType.isComputer(type) || DeviceType.matchesLicenceClass(licenceClass, type));
    }
}
