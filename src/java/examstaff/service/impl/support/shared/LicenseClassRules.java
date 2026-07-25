package examstaff.service.impl.support.shared;

import java.util.Locale;

/**
 * Utility chuẩn hóa và nhận diện hạng GPLX trong phạm vi ExamStaff đang quản lý
 * (A1, A, B1) — quyết định luồng thi mô tô vs ô tô.
 *
 * Vai trò trong luồng examstaff:
 * Hạng mô tô (A1/A) có quy trình thủ tục và phiếu xác nhận khác (không bắt buộc phần thi như B).
 * isMotorcycle và normalizeManaged dùng trước khi tính phí, phân bổ sân,
 * hoặc render dossier — tránh nhận diện sai hạng ngoài tập quản lý.
 *
 * Cách hoạt động:
 * - normalizeManaged — trim/upper; chỉ giữ A1/A/B1; ngoài tập → "".
 * - isMotorcycle — true khi normalize ra A1 hoặc A (B1 không coi là mô tô).
 *
 * Ai gọi:
 * ProcedureFeeQueryServiceImpl, AllocationPassRules, ExaminerAllocationDeskServiceImpl,
 * CandidateDossierServiceImpl, CandidateCallPageServiceImpl — logic theo hạng GPLX.
 */
public final class LicenseClassRules {

    private LicenseClassRules() {
    }

    /**
     * Hạng mô tô (A1 hoặc A).
     * @param licenseCode mã hạng thô
     * @return true nếu sau chuẩn hóa là A1/A
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
     * @param licenseCode mã hạng thô
     * @return A1/A/B1 hoặc ""
     */
    public static String normalizeManaged(String licenseCode) {
        // Validate
        if (licenseCode == null || licenseCode.isBlank()) {
            return "";
        }
        // Result: chỉ giữ A1/A/B1; ngoài tập → rỗng
        return switch (licenseCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1", "A", "B1" -> licenseCode.trim().toUpperCase(Locale.ROOT);
            default -> "";
        };
    }
}
