package Controllers.Staff.ExamStaff;

import Models.AuditLog;

import java.util.Locale;

public final class AuditExportLabels {

    private AuditExportLabels() {
    }

    /** Kiểu thao tác (INSERT / UPDATE / …) — tiếng Việt. */
    public static String formatActionType(AuditLog log) {
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

    public static String formatActionCode(String action) {
        if (action == null || action.isBlank()) {
            return "Khác";
        }
        return switch (action.trim().toUpperCase()) {
            case "INSERT" -> "Thêm mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "IMPORT" -> "Nhập dữ liệu";
            case "EXPORT" -> "Xuất dữ liệu";
            case "ASSIGN" -> "Phân công / Phân bổ";
            default -> action;
        };
    }

    /** Nhóm nghiệp vụ từ EntityName. */
    public static String formatEntityLabel(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "Khác";
        }
        return switch (tableName.trim()) {
            case "Payment" -> "Thu phí thủ tục";
            case "ExamRegistration" -> "Hồ sơ đăng ký thi";
            case "Profile", "Person" -> "Lý lịch thí sinh";
            case "ExamScore" -> "Điểm / Kết quả thi";
            case "Session" -> "Ca thi";
            case "Candidate" -> "Thí sinh";
            case "Session_Examiner" -> "Phân công giám thị";
            default -> tableName;
        };
    }

    /** Mô tả thao tác đầy đủ cho cột Thao tác. */
    public static String formatOperationDetail(AuditLog log) {
        if (log == null) {
            return "";
        }
        String details = log.getDetails();
        if (details != null && !details.isBlank()) {
            return details.trim();
        }
        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            return log.getOldValue().trim();
        }
        return "";
    }
}
