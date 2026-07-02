package util;

import java.util.Locale;

/** Hạng GPLX trong file import vs kỳ thi đã chọn. */
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
        String file = normalize(fileLicense);
        String exam = normalize(examLicense);
        if (file.isEmpty() || exam.isEmpty()) {
            return false;
        }
        if (file.equals(exam)) {
            return true;
        }
        return isCarGroup(file) && isCarGroup(exam);
    }

    private static boolean isCarGroup(String license) {
        return "B".equals(license) || "B1".equals(license) || "B2".equals(license);
    }
}
