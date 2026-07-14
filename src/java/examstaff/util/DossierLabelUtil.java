package examstaff.util;

import java.util.Locale;

/** Tiêu đề / phụ đề phiếu xác nhận thông tin & lệ phí theo hạng GPLX. */
public final class DossierLabelUtil {

    private DossierLabelUtil() {
    }

    /**
     * Tiêu đề cố định của phiếu xác nhận.
     *
     * @param licenseCode hạng GPLX (không dùng trong nội dung hiện tại)
     * @return tiêu đề in hoa
     */
    public static String resolveTitle(String licenseCode) {
        return "PHIẾU XÁC NHẬN THÔNG TIN VÀ LỆ PHÍ THỦ TỤC";
    }

    /**
     * Phụ đề theo mô tô hoặc hạng cụ thể.
     *
     * @param licenseCode mã hạng
     * @return chuỗi phụ đề trong ngoặc
     */
    public static String resolveSubtitle(String licenseCode) {
        if (LicenseClassRules.isMotorcycle(licenseCode)) {
            return "(Thí sinh hạng mô tô - sau khi hoàn tất thủ tục tại bàn quầy)";
        }
        return "(Thí sinh hạng " + (licenseCode != null ? licenseCode.trim() : "")
                + " - sau khi hoàn tất thủ tục tại bàn quầy)";
    }
}
