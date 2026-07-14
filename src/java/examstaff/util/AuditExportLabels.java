package examstaff.util;

import examstaff.dto.user.AuditDTO;

import examstaff.enums.AuditEntity;

import java.util.Locale;

/** Nhãn tiếng Việt cho xuất / hiển thị nhật ký audit. */
public final class AuditExportLabels {

    private AuditExportLabels() {

    }

    /**
     * Gán các nhãn hiển thị (entity / action / chi tiết) lên {@link AuditDTO}.
     *
     * @param log bản ghi audit (null → no-op)
     */
    public static void applyDisplayLabels(AuditDTO log) {

        if (log == null) {

            return;

        }

        log.setEntityLabelVi(formatEntityLabel(log.getTableName()));

        log.setActionLabelVi(formatActionType(log));

        log.setDisplayDetails(formatOperationDetail(log));

    }

    /**
     * Loại thao tác tiếng Việt (ưu tiên suy từ details, rồi action code).
     *
     * @param log bản ghi audit
     * @return nhãn action
     */
    public static String formatActionType(AuditDTO log) {

        if (log == null) {

            return "Khác";

        }

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

        return formatActionCode(log.getAction());

    }

    /**
     * Map mã action thô (INSERT/UPDATE/…) sang nhãn tiếng Việt.
     *
     * @param action mã hoặc cụm action
     * @return nhãn hiển thị
     */
    public static String formatActionCode(String action) {

        if (action == null || action.isBlank()) {

            return "Khác";

        }

        String upper = action.trim().toUpperCase(Locale.ROOT);

        if (upper.contains("WARNING")) {

            return "Cảnh báo";

        }

        if (upper.contains("REMOVE") || upper.contains("DELETE")) {

            return "Xóa / Gỡ";

        }

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

    /** Suy nhãn từ cụm chứa INSERT/UPDATE/… */
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
     *
     * @param tableName tên bảng hoặc mã entity
     * @return nhãn tiếng Việt
     */
    public static String formatEntityLabel(String tableName) {

        if (tableName == null || tableName.isBlank()) {

            return "Khác";

        }

        String trimmed = tableName.trim();

        String upper = trimmed.toUpperCase(Locale.ROOT).replace(" ", "_");

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

        String fromEnum = AuditEntity.resolveLabel(trimmed);

        if (fromEnum != null && !fromEnum.isBlank() && !fromEnum.equals(trimmed)) {

            return normalizeVietnameseEntityAlias(fromEnum);

        }

        return normalizeVietnameseEntityAlias(trimmed);

    }

    /** Đồng bộ alias nhãn entity cũ → thuật ngữ giám thị / thu phí. */
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
     * Chi tiết thao tác: ưu tiên details → reason → old/new value.
     *
     * @param log bản ghi audit
     * @return chuỗi đã chuẩn hóa (có thể rỗng)
     */
    public static String formatOperationDetail(AuditDTO log) {

        if (log == null) {

            return "";

        }

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

    /** Làm gọn chuỗi chi tiết (bỏ ExamId machine tokens, dịch một số key). */
    private static String normalizeOperationDetail(String detail) {

        if (detail == null || detail.isBlank()) {

            return "";

        }

        String normalized = detail.replaceAll("\\s*ExamId=\\d+\\s*-\\s*", " ");
        normalized = normalized.replaceAll("\\s*ExamId=\\d+", "");
        normalized = normalized.replaceAll("(?i)\\buserId=(\\d+)", "mã người dùng $1");
        normalized = normalized.replaceAll("(?i)\\bslot=([\\d:]+)", "phân công $1");
        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;

    }

}
