package util.examstaff;

import java.util.Locale;

public final class LicenseClassRules {

    private LicenseClassRules() {
    }

    public static boolean isMotorcycle(String licenseCode) {
        String code = normalizeManaged(licenseCode);
        if (code.isEmpty()) {
            return false;
        }
        return "A1".equals(code) || "A".equals(code);
    }

    public static String normalizeManaged(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return "";
        }
        return switch (licenseCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1", "A", "B1" -> licenseCode.trim().toUpperCase(Locale.ROOT);
            default -> "";
        };
    }
}
