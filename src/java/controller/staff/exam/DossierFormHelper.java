package controller.staff.exam;

import java.util.Locale;

public final class DossierFormHelper {

    private DossierFormHelper() {
    }

    // Xac dinh title
    public static String resolveTitle(String licenseCode) {
        return "PHIẾU XÁC NHẬN THÔNG TIN VÀ LỆ PHÍ THỦ TỤC";
    }
    // Xac dinh subtitle

    public static String resolveSubtitle(String licenseCode) {
        if (isMotorcycleGroup(licenseCode)) {
            return "(Thí sinh hạng mô tô — sau khi hoàn tất thủ tục tại bàn quầy)";
        }
        return "(Thí sinh hạng " + (licenseCode != null ? licenseCode.trim() : "")
                + " — sau khi hoàn tất thủ tục tại bàn quầy)";
    // Kiem tra motorcycle group
    }

    private static boolean isMotorcycleGroup(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String code = licenseCode.trim().toUpperCase(Locale.ROOT);
        return "A1".equals(code) || "A".equals(code) || "A2".equals(code) || "B1".equals(code);
    }
}
