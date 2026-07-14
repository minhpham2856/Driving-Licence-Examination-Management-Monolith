package registrant.util;

import registrant.enums.AuditEntityLabels;
import registrant.dto.AuditLogEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuditLogViewHelper {

    private static final Pattern SBD_PATTERN =
            Pattern.compile("SBD\\s+([A-Za-z0-9]+-\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private AuditLogViewHelper() {
    }

    public static List<Map<String, Object>> toViewRows(AuditLogEntry log, Map<Integer, String> sbdByRecordId) {
        List<AuditChangeDetails.FieldChange> changes = AuditChangeDetails.parseChanges(log.getDetails());
        if (changes.size() <= 1) {
            return List.of(toViewRow(log, sbdByRecordId));
        }
        List<Map<String, Object>> rows = new ArrayList<>(changes.size());
        for (AuditChangeDetails.FieldChange change : changes) {
            rows.add(toViewRowForFieldChange(log, sbdByRecordId, change));
        }
        return rows;
    }

    public static Map<String, Object> toViewRow(AuditLogEntry log, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String reason = normalizeReason(log);

        AuditChangeDetails.DisplayColumns columns = AuditChangeDetails.toDisplayColumns(
                log.getDetails(), log.getOldValue(), log.getNewValue());
        boolean hasFieldChanges = columns.info() != null;

        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", AuditEntityLabels.toVietnamese(log.getTableName()));
        row.put("sbd", sbd);
        row.put("newValueClass", mapNewValueClass(action));
        row.put("multiline", columns.multiline());

        if (hasFieldChanges) {
            row.put("info", columns.info());
            row.put("oldValue", columns.oldValue());
            row.put("newValue", columns.newValue());
            row.put("reason", nullToDash(reason));
        } else if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            row.put("info", buildChangeInfo(log, sbd));
            row.put("oldValue", log.getOldValue());
            row.put("newValue", nullToDash(log.getNewValue()));
            row.put("reason", nullToDash(reason));
            row.put("multiline", log.getOldValue().contains(";"));
        } else {
            row.put("info", "-");
            row.put("oldValue", null);
            row.put("newValue", nullToDash(log.getNewValue()));
            row.put("reason", nullToDash(reason));
        }
        return row;
    }

    private static Map<String, Object> toViewRowForFieldChange(AuditLogEntry log, Map<Integer, String> sbdByRecordId,
            AuditChangeDetails.FieldChange change) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String reason = normalizeReason(log);

        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", AuditEntityLabels.toVietnamese(log.getTableName()));
        row.put("sbd", sbd);
        row.put("newValueClass", mapNewValueClass(action));
        row.put("multiline", false);
        row.put("info", "Thay đổi " + change.field().toLowerCase());
        row.put("oldValue", nullToDash(change.oldValue()));
        row.put("newValue", nullToDash(change.newValue()));
        row.put("reason", nullToDash(reason));
        return row;
    }

    public static String resolveSbd(AuditLogEntry log, Map<Integer, String> sbdByRecordId) {
        for (String text : new String[] {log.getNewValue(), log.getOldValue(), log.getReason(), log.getDetails()}) {
            String extracted = extractSbdFromText(text);
            if (extracted != null) {
                return extracted;
            }
        }
        if (log.getRecordId() != null && log.getRecordId() > 0 && sbdByRecordId != null) {
            String mapped = sbdByRecordId.get(log.getRecordId());
            if (mapped != null && !mapped.isBlank()) {
                return mapped;
            }
        }
        return "-";
    }

    private static String extractSbdFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = SBD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String normalizeReason(AuditLogEntry log) {
        String reason = log.getReason();
        if (reason == null || reason.isBlank()) {
            return null;
        }
        if (log.getDetails() == null || log.getDetails().isBlank()) {
            if (log.getOldValue() == null || log.getOldValue().isBlank()) {
                if (log.getNewValue() != null && reason.equals(log.getNewValue())) {
                    return null;
                }
            }
        }
        return reason;
    }

    private static String buildChangeInfo(AuditLogEntry log, String sbd) {
        String entity = AuditEntityLabels.toVietnamese(log.getTableName());
        String action = log.getAction() != null ? log.getAction().toUpperCase() : "UPDATE";
        String sbdSuffix = "-".equals(sbd) ? "" : " SBD " + sbd;
        return switch (action) {
            case "WARNING" -> "Cảnh báo" + sbdSuffix;
            case "INSERT" -> "Thêm " + entity.toLowerCase() + sbdSuffix;
            case "DELETE" -> "Xóa " + entity.toLowerCase() + sbdSuffix;
            default -> "Cập nhật " + entity.toLowerCase() + sbdSuffix;
        };
    }

    private static String mapActionLabel(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "Thêm";
            case "DELETE" -> "Xóa";
            case "EXPORT" -> "Xuất";
            case "ASSIGN" -> "Phân công";
            case "IMPORT" -> "Nhập";
            case "WARNING" -> "Cảnh báo";
            case "SYSTEM" -> "Hệ thống";
            case "APPROVE" -> "Duyệt";
            case "REJECT" -> "Từ chối";
            case "REQUEST" -> "Gửi duyệt";
            case "UPLOAD" -> "Tải lên";
            default -> "Cập nhật";
        };
    }

    private static String mapActionBadge(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "audit-badge--insert";
            case "DELETE" -> "audit-badge--delete";
            case "EXPORT" -> "audit-badge--export";
            case "ASSIGN" -> "audit-badge--assign";
            case "IMPORT" -> "audit-badge--import";
            case "WARNING" -> "audit-badge--warning";
            case "SYSTEM" -> "audit-badge--system";
            case "APPROVE" -> "audit-badge--approve";
            case "REJECT" -> "audit-badge--delete";
            case "REQUEST" -> "audit-badge--import";
            case "UPLOAD" -> "audit-badge--insert";
            default -> "audit-badge--update";
        };
    }

    private static String mapNewValueClass(String action) {
        if ("DELETE".equalsIgnoreCase(action)) {
            return "audit-td--old";
        }
        return "audit-td--new";
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
