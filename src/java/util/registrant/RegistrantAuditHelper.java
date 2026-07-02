package util.registrant;

import util.AuditLogHelper;
import util.AuditLogViewHelper;
import enums.AuditEntityLabels;
import model.user.AuditLog;
import dto.registrant.RegistrantTrackingLog;
import jakarta.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Audit cổng thí sinh: ghi log thao tác và map bản ghi Audit sang dòng theo dõi hồ sơ.
 */
public final class RegistrantAuditHelper {

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));

    private RegistrantAuditHelper() {
    }

    public static void logDocumentUpload(HttpSession session, int profileId, String documentType, String fileName) {
        String label = documentType != null ? documentType : "Document";
        AuditLogHelper.persistForEntity(session, "Document", "UPLOAD",
                "Tải lên tài liệu " + label + (fileName != null ? ": " + fileName : ""),
                "Đã tải lên", profileId);
    }

    public static void logDocumentApprovalRequest(HttpSession session, int profileId, String note) {
        AuditLogHelper.persistForEntity(session, "Document", "REQUEST",
                "Thí sinh gửi yêu cầu duyệt hồ sơ tài liệu",
                note != null && !note.isBlank() ? note.trim() : "Gửi duyệt", profileId);
    }

    public static void logDocumentDelete(HttpSession session, int profileId, String documentType, String fileName) {
        String label = documentType != null ? documentType : "Document";
        AuditLogHelper.persistForEntity(session, "Document", "DELETE",
                "Xóa tài liệu " + label + (fileName != null && !fileName.isBlank() ? ": " + fileName : ""),
                "Đã xóa", profileId);
    }

    public static void logProfileUpdate(HttpSession session, int profileId, String summary) {
        AuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                summary != null ? summary : "Cập nhật hồ sơ cá nhân",
                "Đã cập nhật", profileId);
    }

    public static void logProfileCreate(HttpSession session, int profileId) {
        AuditLogHelper.persistForEntity(session, "Profile", "INSERT",
                "Tạo hồ sơ cá nhân trên hệ thống", "Đã tạo", profileId);
    }

    public static void logExamRegistration(HttpSession session, int profileId, String examLabel) {
        AuditLogHelper.persistForEntity(session, "ExamRegistration", "INSERT",
                "Đăng ký đợt thi: " + (examLabel != null ? examLabel : "—"),
                "PreRegistered", profileId);
    }

    public static void logExamCancellationRequest(HttpSession session, int profileId,
            String examLabel, String reason) {
        String detail = "Yêu cầu hủy đăng ký: " + (examLabel != null ? examLabel : "—");
        if (reason != null && !reason.isBlank()) {
            detail += ". Lý do: " + reason.trim();
        }
        AuditLogHelper.persistForEntity(session, "ExamRegistration", "REQUEST", detail,
                "CancelRequested", profileId);
    }

    public static void logPasswordChange(HttpSession session, int userId) {
        AuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                "Đổi mật khẩu tài khoản", "Đã đổi mật khẩu", userId);
    }

    public static void logAccountDeactivate(HttpSession session, int userId) {
        AuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                "Vô hiệu hoá tài khoản thí sinh", "Đã vô hiệu hoá", userId);
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
