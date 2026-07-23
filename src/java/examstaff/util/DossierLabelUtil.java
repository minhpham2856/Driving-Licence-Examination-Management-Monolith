package examstaff.util;

import java.util.Locale;

public final class DossierLabelUtil {

    private DossierLabelUtil() {
    }

    public static String resolveTitle(String licenseCode) {
        return "PHIẾU XÁC NHẬN THÔNG TIN VÀ LỆ PHÍ THỦ TỤC";
    }

    public static String resolveSubtitle(String licenseCode) {
        if (LicenseClassRules.isMotorcycle(licenseCode)) {
            return "(Thí sinh hạng mô tô - sau khi hoàn tất thủ tục tại bàn quầy)";
        }
        return "(Thí sinh hạng " + (licenseCode != null ? licenseCode.trim() : "")
                + " - sau khi hoàn tất thủ tục tại bàn quầy)";
    }
}
