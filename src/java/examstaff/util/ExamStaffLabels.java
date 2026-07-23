package examstaff.util;

import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AuditDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.enums.AuditEntity;

import java.util.Locale;

/**
 * Utility nhãn và chuỗi hiển thị tiếng Việt cho exam staff — audit, báo cáo, thu phí,
 * phiếu xác nhận thủ tục. Pure static; một số heuristic đọc chuỗi details/action thô.
 *
 * Vai trò trong luồng examstaff:
 * Dữ liệu audit/ báo cáo lưu mã EN hoặc tên bảng SQL; UI cần nhãn VI đọc được.
 * {@link #applyDisplayLabels} gắn entity/action/details lên {@link AuditDTO}.
 * Nhóm report ({@link #formatTheoryResult}, {@link #formatFinalResult}, …) và procedure
 * ({@link #formatFeeAmount}, {@link #formatAutoAllocateDetail}) phục vụ JSP/export.
 *
 * Nhóm chức năng:
 * - Audit — {@link #formatEntityLabel}, {@link #formatActionType}, {@link #formatOperationDetail}
 *       (dùng {@link examstaff.enums.AuditEntity}).
 * - Báo cáo — kết quả LT/TH/tổng, yes/no, {@link #safeFileToken} cho tên file export.
 * - Thủ tục — format tiền lệ phí, mô tả auto-allocate sau thu phí, tiêu đề phiếu xác nhận.
 *
 * Ai gọi:
 * {@code StaffAuditPageServiceImpl}, {@code StaffAuditExportServiceImpl}, {@code ReportServlet},
 * {@code ProcedureServlet}, {@code CandidateDossierServiceImpl} — mọi màn cần text VI từ DTO thô.
 */
public final class ExamStaffLabels {

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamStaffLabels() {
    }

    // -------------------------------------------------------------------------
    // Audit export / display
    // -------------------------------------------------------------------------

    /**
     * Gán các nhãn hiển thị (entity / action / chi tiết) lên {@link AuditDTO}.
     * <p>
     * Thứ tự: entity label → action type → display details. null log → no-op.
     * @param log bản ghi audit (null → no-op)
     */
    public static void applyDisplayLabels(AuditDTO log) {
        if (log == null) {
            return;
        }
        // Bước 1–3: gắn ba nhãn UI từ dữ liệu thô
        log.setEntityLabelVi(formatEntityLabel(log.getTableName()));
        log.setActionLabelVi(formatActionType(log));
        log.setDisplayDetails(formatOperationDetail(log));
    }

    /**
     * Loại thao tác tiếng Việt (ưu tiên suy từ details, rồi action code).
     * <p>
     * Luồng: null → “Khác”; quét details (RESET / phân bổ / thu phí / import);
     * không khớp → {@link #formatActionCode}.
     * @param log bản ghi audit
     * @return nhãn action
     */
    public static String formatActionType(AuditDTO log) {
        if (log == null) {
            return "Khác";
        }
        // Bước 1: ưu tiên suy từ nội dung details
        String details = log.getDetails();
        if (details != null) {
            String upper = details.toUpperCase(Locale.ROOT);
            if (upper.contains("RESET") || details.toLowerCase(Locale.ROOT).contains("xóa hồ sơ thủ tục")) {
                return "Đặt lại thủ tục";
            }
            if (upper.contains("PHÂN BỔ") || upper.contains("ALLOCATE")) {
                return "Phân bổ";
            }
            if (upper.contains("THU PHÍ") || upper.contains("THANH TOÁN")) {
                return "Thu phí";
            }
            if (upper.contains("IMPORT") || details.toLowerCase(Locale.ROOT).contains("nhập")) {
                return "Nhập dữ liệu";
            }
        }
        // Bước 2: fallback mã action
        return formatActionCode(log.getAction());
    }

    /**
     * Map mã action thô (INSERT/UPDATE/…) sang nhãn tiếng Việt.
     * <p>
     * Luồng: blank → “Khác”; WARNING / REMOVE|DELETE → nhãn tương ứng;
     * switch exact code; default → {@link #formatActionCodeFromPhrase}.
     * @param action mã hoặc cụm action
     * @return nhãn hiển thị
     */
    public static String formatActionCode(String action) {
        if (action == null || action.isBlank()) {
            return "Khác";
        }
        String upper = action.trim().toUpperCase(Locale.ROOT);
        // Bước 1: từ khóa chứa
        if (upper.contains("WARNING")) {
            return "Cảnh báo";
        }
        if (upper.contains("REMOVE") || upper.contains("DELETE")) {
            return "Xóa / Gỡ";
        }
        // Bước 2: khớp đúng mã chuẩn
        return switch (upper) {
            case "INSERT" -> "Thêm mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "IMPORT" -> "Nhập dữ liệu";
            case "EXPORT" -> "Xuất dữ liệu";
            case "ASSIGN" -> "Phân công / Phân bổ";
            default -> formatActionCodeFromPhrase(action.trim());
        };
    }

    /**
     * Suy nhãn từ cụm chứa INSERT/UPDATE/IMPORT/ASSIGN/EXPORT; không khớp → trả nguyên action.
     * @param action chuỗi action thô đã trim
     * @return nhãn tiếng Việt hoặc chuỗi gốc
     */
    private static String formatActionCodeFromPhrase(String action) {
        String upper = action.toUpperCase(Locale.ROOT);
        if (upper.contains("INSERT")) {
            return "Thêm mới";
        }
        if (upper.contains("UPDATE")) {
            return "Cập nhật";
        }
        if (upper.contains("IMPORT")) {
            return "Nhập dữ liệu";
        }
        if (upper.contains("ASSIGN")) {
            return "Phân công / Phân bổ";
        }
        if (upper.contains("EXPORT")) {
            return "Xuất dữ liệu";
        }
        return action;
    }

    /**
     * Nhãn thực thể theo tên bảng / alias enum.
     * <p>
     * Luồng: blank → “Khác”; switch mã bảng; rồi {@link AuditEntity#resolveLabel};
     * cuối cùng {@link #normalizeVietnameseEntityAlias}.
     * @param tableName tên bảng hoặc mã entity
     * @return nhãn tiếng Việt
     */
    public static String formatEntityLabel(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "Khác";
        }
        String trimmed = tableName.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT).replace(" ", "_");
        // Bước 1: map tên bảng kỹ thuật phổ biến
        String mapped = switch (upper) {
            case "PAYMENT" -> "Thu phí thủ tục";
            case "EXAMREGISTRATION" -> "Hồ sơ đăng ký thi";
            case "PROFILE", "PERSON" -> "Lý lịch thí sinh";
            case "EXAMSCORE" -> "Điểm / Kết quả thi";
            case "SESSION" -> "Điều hành ca thi";
            case "CANDIDATE", "EXAMENROLLMENT" -> "Thí sinh";
            case "EXAMINERSCHEDULE", "SESSION_EXAMINER" -> "Phân công giám thị";
            case "SESSION_EXAMINERAREA" -> "Phân công phòng giám thị";
            case "EXAMDEVICE" -> "Thiết bị thi";
            case "SCOREENTRYQUEUE" -> "Hàng đợi nhập điểm";
            case "CANDIDATECALL" -> "Gọi thí sinh";
            default -> null;
        };
        if (mapped != null) {
            return mapped;
        }
        // Bước 2: thử enum AuditEntity
        String fromEnum = AuditEntity.resolveLabel(trimmed);
        if (fromEnum != null && !fromEnum.isBlank() && !fromEnum.equals(trimmed)) {
            return normalizeVietnameseEntityAlias(fromEnum);
        }
        // Bước 3: chuẩn hóa alias tiếng Việt cũ
        return normalizeVietnameseEntityAlias(trimmed);
    }

    /**
     * Đồng bộ alias nhãn entity cũ → thuật ngữ giám thị / thu phí hiện dùng trên UI.
     * @param label nhãn gốc
     * @return nhãn đã chuẩn hóa (blank → “Khác”)
     */
    private static String normalizeVietnameseEntityAlias(String label) {
        if (label == null || label.isBlank()) {
            return "Khác";
        }
        return switch (label.trim()) {
            case "Phân công sát hạch viên" -> "Phân công giám thị";
            case "Phân công phòng sát hạch viên" -> "Phân công phòng giám thị";
            case "Thanh toán" -> "Thu phí thủ tục";
            case "Điểm thi", "Kết quả thi" -> "Điểm / Kết quả thi";
            case "Ca thi" -> "Điều hành ca thi";
            case "Thí sinh", "Hồ sơ đăng ký thi" -> label.trim();
            default -> label.trim();
        };
    }

    /**
     * Chi tiết thao tác: ưu tiên details → reason → old/new value; mỗi nguồn qua normalize.
     * @param log bản ghi audit
     * @return chuỗi đã chuẩn hóa (có thể rỗng)
     */
    public static String formatOperationDetail(AuditDTO log) {
        if (log == null) {
            return "";
        }
        // Bước 1–4: lấy nguồn đầu tiên có nội dung
        String details = log.getDetails();
        if (details != null && !details.isBlank()) {
            return normalizeOperationDetail(details.trim());
        }
        if (log.getReason() != null && !log.getReason().isBlank()) {
            return normalizeOperationDetail(log.getReason().trim());
        }
        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            return normalizeOperationDetail(log.getOldValue().trim());
        }
        if (log.getNewValue() != null && !log.getNewValue().isBlank()) {
            return normalizeOperationDetail(log.getNewValue().trim());
        }
        return "";
    }

    /**
     * Làm gọn chuỗi chi tiết: bỏ token ExamId máy, dịch userId/slot, gộp khoảng trắng.
     * @param detail chuỗi chi tiết thô
     * @return chuỗi đã chuẩn hóa (có thể rỗng)
     */
    private static String normalizeOperationDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return "";
        }
        // Bước 1: tách ExamId khỏi câu mô tả người đọc
        String normalized = detail.replaceAll("\\s*ExamId=\\d+\\s*-\\s*", " ");
        normalized = normalized.replaceAll("\\s*ExamId=\\d+", "");
        // Bước 2: dịch một số key kỹ thuật → tiếng Việt
        normalized = normalized.replaceAll("(?i)\\buserId=(\\d+)", "mã người dùng $1");
        normalized = normalized.replaceAll("(?i)\\bslot=([\\d:]+)", "phân công $1");
        // Bước 3: gộp khoảng trắng thừa
        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }

    // -------------------------------------------------------------------------
    // Report export
    // -------------------------------------------------------------------------

    /**
     * Đổi cờ đạt/trượt phần thi thành nhãn hiển thị.
     * @param passed {@code passed}/{@code failed}/{@code none}/khác
     * @return nhãn tiếng Việt hoặc chuỗi gốc
     */
    public static String formatSectionResult(String passed) {
        if (passed == null || "none".equalsIgnoreCase(passed.trim())) {
            return "Chưa thi";
        }
        if ("passed".equalsIgnoreCase(passed)) {
            return "Đạt";
        }
        if ("failed".equalsIgnoreCase(passed)) {
            return "Trượt";
        }
        return passed;
    }

    /**
     * Kết quả lý thuyết (kể cả bảo lưu khi {@code skipsTheory}).
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
    public static String formatTheoryResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        // Bảo lưu phần lý thuyết → không lấy kết quả thi
        if (reg.isSkipsTheory()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getTheoryPassed());
    }

    /**
     * Kết quả thực hành (kể cả bảo lưu khi {@code skipsPractical}).
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
    public static String formatPracticalResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSkipsPractical()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getPracticalPassed());
    }

    /**
     * Kết quả tổng: đình chỉ → vắng → chưa xong → đạt / trượt.
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
    public static String formatFinalResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        // Ưu tiên trạng thái đặc biệt trước kết quả pass/fail
        if (reg.isSuspended()) {
            return "Đình chỉ";
        }
        if (reg.isAbsent()) {
            return "Vắng";
        }
        if (!reg.isExamFinished()) {
            return "Chưa xong";
        }
        return reg.isFinalPass() ? "Đạt" : "Trượt";
    }

    /**
     * Boolean → Có / Chưa (dùng cột báo cáo / checklist).
     * @param value giá trị
     * @return {@code "Có"} hoặc {@code "Chưa"}
     */
    public static String yesNo(boolean value) {
        return value ? "Có" : "Chưa";
    }

    /**
     * Chuẩn hóa token an toàn cho tên file: ký tự lạ → {@code _}, gộp nhiều {@code _}.
     * @param raw chuỗi gốc (blank → {@code "ca_thi"})
     * @return token file
     */
    public static String safeFileToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return "ca_thi";
        }
        return raw.trim()
                .replaceAll("[^A-Za-z0-9\\-]", "_")
                .replaceAll("_+", "_");
    }

    // -------------------------------------------------------------------------
    // Procedure payment
    // -------------------------------------------------------------------------

    /**
     * Format số tiền lệ phí; thiếu preview hoặc total ≤ 0 thì dùng mặc định 200.000 đ.
     * @param feePreview kết quả tính phí (có thể null)
     * @return chuỗi tiền kèm đơn vị {@code đ}
     */
    public static String formatFeeAmount(ProcedureFeeResultDTO feePreview) {
        if (feePreview == null || feePreview.getFeeTotal() <= 0) {
            return "200,000 đ";
        }
        return String.format("%,.0f đ", feePreview.getFeeTotal());
    }

    /**
     * Cụm mô tả kết quả tự phân bổ phòng sau khi thu phí.
     * <p>
     * Có allocatedCount &gt; 0 → câu thành công; có errorMsg → ngoặc lỗi;
     * còn lại → cảnh báo chưa phân phòng.
     * @param allocResult kết quả auto-allocate (có thể null)
     * @return chuỗi bổ sung (thành công / lỗi / cảnh báo)
     */
    public static String formatAutoAllocateDetail(AllocationActionResultDTO allocResult) {
        if (allocResult != null && allocResult.getAllocatedCount() > 0) {
            return " và tự động phân bổ vào phòng thi";
        }
        if (allocResult != null && allocResult.getErrorMsg() != null && !allocResult.getErrorMsg().isBlank()) {
            return " (" + allocResult.getErrorMsg().trim() + ")";
        }
        return " (chưa phân được phòng - kiểm tra phân công sát hạch viên phòng lý thuyết)";
    }

    // -------------------------------------------------------------------------
    // Dossier (phiếu xác nhận)
    // -------------------------------------------------------------------------

    /**
     * Tiêu đề cố định của phiếu xác nhận thông tin và lệ phí thủ tục.
     * @param licenseCode hạng GPLX (giữ tham số API; không dùng trong nội dung hiện tại)
     * @return tiêu đề in hoa
     */
    public static String resolveTitle(String licenseCode) {
        return "PHIẾU XÁC NHẬN THÔNG TIN VÀ LỆ PHÍ THỦ TỤC";
    }

    /**
     * Phụ đề theo mô tô ({@link #isMotorcycleLicense}) hoặc hạng cụ thể khác.
     * @param licenseCode mã hạng
     * @return chuỗi phụ đề trong ngoặc
     */
    public static String resolveSubtitle(String licenseCode) {
        if (isMotorcycleLicense(licenseCode)) {
            return "(Thí sinh hạng mô tô - sau khi hoàn tất thủ tục tại bàn quầy)";
        }
        return "(Thí sinh hạng " + (licenseCode != null ? licenseCode.trim() : "")
                + " - sau khi hoàn tất thủ tục tại bàn quầy)";
    }

    /**
     * Cùng ngữ nghĩa {@code LicenseClassRules.isMotorcycle} — tránh util phụ thuộc BLL.
     * Chỉ A1 / A được coi là mô tô (B1 được nhận diện trong switch nhưng không trả true).
     * @param licenseCode mã hạng GPLX
     * @return {@code true} nếu A1 hoặc A
     */
    private static boolean isMotorcycleLicense(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String code = switch (licenseCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1", "A", "B1" -> licenseCode.trim().toUpperCase(Locale.ROOT);
            default -> "";
        };
        return "A1".equals(code) || "A".equals(code);
    }
}
