package util;

import java.util.Locale;

/** Hạng GPLX trong file import vs kỳ thi đã chọn (A1, A, B1). */
public final class CandidateImportLicenseUtil {

    private CandidateImportLicenseUtil() {
    }

    public static String normalize(String licenseCode) {
        if (licenseCode == null) {
            return "";
        }
        return licenseCode.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean matchesExam(String fileLicense, String examLicense) {
        String file = toManaged(normalize(fileLicense));
        String exam = toManaged(normalize(examLicense));
        return !file.isEmpty() && file.equals(exam);
    }

    private static String toManaged(String license) {
        return switch (license) {
            case "A2" -> "A";
            case "B", "B2" -> "B1";
            case "A1", "A", "B1" -> license;
            default -> "";
        };
    }
}
