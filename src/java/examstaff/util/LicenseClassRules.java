package examstaff.util;

import java.util.Locale;

/** Chuẩn hóa / nhận diện hạng GPLX đang quản lý (A1, A, B1). */
public final class LicenseClassRules {

    private LicenseClassRules() {
    }

    /**
     * Hạng mô tô (A1 hoặc A).
     *
     * @param licenseCode mã hạng thô
     * @return {@code true} nếu sau chuẩn hóa là A1/A
     */
    public static boolean isMotorcycle(String licenseCode) {
        String code = normalizeManaged(licenseCode);
        if (code.isEmpty()) {
            return false;
        }
        return "A1".equals(code) || "A".equals(code);
    }

    /**
     * Chuẩn hóa mã hạng về tập quản lý; ngoài tập → chuỗi rỗng.
     *
     * @param licenseCode mã hạng thô
     * @return {@code A1}/{@code A}/{@code B1} hoặc {@code ""}
     */
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
