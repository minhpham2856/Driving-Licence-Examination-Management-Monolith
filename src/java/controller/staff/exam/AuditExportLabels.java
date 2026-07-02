package controller.staff.exam;

import dto.user.AuditDTO;

import enums.AuditEntity;

import java.util.Locale;

public final class AuditExportLabels {

    private AuditExportLabels() {

    }

    // apply display labels
    public static void applyDisplayLabels(AuditDTO log) {

        if (log == null) {

            return;

        }

        log.setEntityLabelVi(formatEntityLabel(log.getTableName()));

        log.setActionLabelVi(formatActionType(log));

        log.setDisplayDetails(formatOperationDetail(log));

    }
    // format action type

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

    // format action code
    }

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
    // format action code from phrase

    }

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

    // format entity label
        return action;

    }

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

            case "SESSION" -> "Ca thi";

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
    // normalize vietnamese entity alias

        return normalizeVietnameseEntityAlias(trimmed);

    }

    private static String normalizeVietnameseEntityAlias(String label) {

        if (label == null || label.isBlank()) {

            return "Khác";

        }

        return switch (label.trim()) {

            case "Phân công sát hạch viên" -> "Phân công giám thị";

            case "Phân công phòng sát hạch viên" -> "Phân công phòng giám thị";

            case "Thanh toán" -> "Thu phí thủ tục";

            case "Điểm thi", "Kết quả thi" -> "Điểm / Kết quả thi";

            case "Thí sinh", "Hồ sơ đăng ký thi" -> label.trim();

    // format operation detail
            default -> label.trim();

        };

    }

    public static String formatOperationDetail(AuditDTO log) {

        if (log == null) {

            return "";

        }

        String details = log.getDetails();

        if (details != null && !details.isBlank()) {

            return details.trim();

        }

        if (log.getReason() != null && !log.getReason().isBlank()) {

            return log.getReason().trim();

        }

        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {

            return log.getOldValue().trim();

        }

        if (log.getNewValue() != null && !log.getNewValue().isBlank()) {

            return log.getNewValue().trim();

        }

        return "";

    }

}
