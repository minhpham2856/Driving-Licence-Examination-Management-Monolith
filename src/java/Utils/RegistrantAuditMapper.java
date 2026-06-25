package Utils;

import Constants.AuditEntityLabels;
import Models.AuditLog;
import Models.RegistrantTrackingLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Chuyển bản ghi Audit sang dòng hiển thị trên màn theo dõi hồ sơ thí sinh.
 */
public final class RegistrantAuditMapper {

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));

    private RegistrantAuditMapper() {
    }

    public static List<RegistrantTrackingLog> toTrackingLogs(List<AuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return Collections.emptyList();
        }
        List<RegistrantTrackingLog> rows = new ArrayList<>(auditLogs.size());
        for (AuditLog log : auditLogs) {
            rows.add(toTrackingLog(log));
        }
        return rows;
    }

    public static RegistrantTrackingLog toTrackingLog(AuditLog log) {
        RegistrantTrackingLog row = new RegistrantTrackingLog();
        String action = log.getAction() != null ? log.getAction().toUpperCase(Locale.ROOT) : "UPDATE";
        row.setTimestamp(log.getChangedAt());
        row.setEventTitle(buildEventTitle(log, action));
        row.setActorRole(log.getChangerName() != null && !log.getChangerName().isBlank()
                ? log.getChangerName() : "Hệ thống");
        row.setStatusClass(mapStatusClass(action));
        row.setStatusLabel(mapStatusLabel(action));
        row.setRemarks(buildRemarks(log));
        row.setCategory(RegistrantTrackingCategories.categoryFromAuditAction(action, log.getTableName()));
        return row;
    }

    public static List<Map<String, Object>> toAuditViewRows(List<AuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuditLog log : auditLogs) {
            for (Map<String, Object> viewRow : AuditLogViewHelper.toViewRows(log, Collections.emptyMap())) {
                Map<String, Object> enriched = new LinkedHashMap<>(viewRow);
                enriched.put("changedAt", log.getChangedAt());
                enriched.put("timeFormatted", log.getChangedAt() != null
                        ? TIME_FMT.format(log.getChangedAt()) : "—");
                rows.add(enriched);
            }
        }
        return rows;
    }

    private static String buildEventTitle(AuditLog log, String action) {
        String entity = AuditEntityLabels.toVietnamese(log.getTableName());
        return switch (action) {
            case "INSERT" -> "Thêm " + entity.toLowerCase(Locale.ROOT);
            case "DELETE" -> "Xóa " + entity.toLowerCase(Locale.ROOT);
            case "REQUEST" -> "Gửi duyệt " + entity.toLowerCase(Locale.ROOT);
            case "APPROVE" -> "Phê duyệt " + entity.toLowerCase(Locale.ROOT);
            case "REJECT" -> "Từ chối " + entity.toLowerCase(Locale.ROOT);
            case "UPLOAD" -> "Tải lên " + entity.toLowerCase(Locale.ROOT);
            default -> "Cập nhật " + entity.toLowerCase(Locale.ROOT);
        };
    }

    private static String buildRemarks(AuditLog log) {
        if (log.getNewValue() != null && !log.getNewValue().isBlank()) {
            if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
                return log.getOldValue() + " → " + log.getNewValue();
            }
            return log.getNewValue();
        }
        if (log.getReason() != null && !log.getReason().isBlank()) {
            return log.getReason();
        }
        if (log.getDetails() != null && !log.getDetails().isBlank()) {
            return log.getDetails();
        }
        return "—";
    }

    private static String mapStatusLabel(String action) {
        return switch (action) {
            case "INSERT", "UPLOAD" -> "Thêm mới";
            case "DELETE", "REJECT" -> "Từ chối";
            case "REQUEST" -> "Chờ duyệt";
            case "APPROVE" -> "Thành công";
            case "WARNING" -> "Cảnh báo";
            default -> "Cập nhật";
        };
    }

    private static String mapStatusClass(String action) {
        return switch (action) {
            case "APPROVE", "INSERT", "UPLOAD" -> "approved";
            case "REQUEST" -> "pending";
            case "REJECT", "DELETE", "WARNING" -> "rejected";
            default -> "info";
        };
    }
}
