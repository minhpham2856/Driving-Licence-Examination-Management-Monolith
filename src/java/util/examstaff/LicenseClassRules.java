package util.examstaff;

import java.util.Locale;

public final class LicenseClassRules {

    private LicenseClassRules() {
    }

    public static boolean isMotorcycle(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String code = licenseCode.trim().toUpperCase(Locale.ROOT);
        return "A1".equals(code) || "A".equals(code);
    }

    public static boolean requiresRoadTest(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String lc = licenseCode.trim().toUpperCase(Locale.ROOT);
        return "B1".equals(lc) || "B".equals(lc) || "B2".equals(lc);
    }
}
