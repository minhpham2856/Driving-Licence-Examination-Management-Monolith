package examstaff.util;

import examstaff.enums.DeviceType;
import examstaff.enums.DeviceStatus;

import java.util.Map;

public final class FormatUtil {

    private FormatUtil() {
    }

    // --- mainTest methods ---

    public static String text(String str) {
        return (str == null || str.trim().isBlank()) ? null : str.trim();
    }

    public static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Integer toInteger(String str) {
        if (str == null || str.trim().isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- CleanMyBranch methods ---

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
