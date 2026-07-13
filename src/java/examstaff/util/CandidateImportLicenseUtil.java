package examstaff.util;

import java.util.Locale;

/** Hạng GPLX trong file import vs kỳ thi đã chọn (A1, A, B1). */
public final class CandidateImportLicenseUtil {

    private CandidateImportLicenseUtil() {
    }

    public static String normalize(String licenseCode) {
        return normalizeRaw(licenseCode);
    }

    public static boolean matchesExam(String fileLicense, String examLicense) {
        String file = toManaged(normalizeRaw(fileLicense));
        String exam = toManaged(normalizeRaw(examLicense));
        return !file.isEmpty() && file.equals(exam);
    }

    public static String normalizeManaged(String licenseCode) {
        return toManaged(normalizeRaw(licenseCode));
    }

    private static String normalizeRaw(String licenseCode) {
        if (licenseCode == null) {
            return "";
        }
        return licenseCode.trim().toUpperCase(Locale.ROOT);
    }

    private static String toManaged(String license) {
        return switch (license) {
            case "A1", "A", "B1" -> license;
            default -> "";
        };
    }
}
