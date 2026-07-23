package registrant.util;

import registrant.enums.AuditEntityLabels;
import registrant.dto.AuditLogEntry;
import registrant.dto.RegistrantTrackingLog;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Facade audit cổng thí sinh: ghi log thao tác và map bản ghi {@code Audit} sang {@link registrant.dto.RegistrantTrackingLog}.
 * <p>
 * Bao bọc {@link RegistrantAuditLogHelper} với message tiếng Việt cho upload, gửi duyệt, xóa tài liệu, cập nhật profile;
 * {@link #toTrackingLogs} dùng trên trang track-profile.
 */
public final class RegistrantAuditHelper {

    private RegistrantAuditHelper() {
    }

    /** Ghi audit khi thí sinh tải lên tài liệu hồ sơ. */
    public static void logDocumentUpload(HttpSession session, int profileId, String documentType, String fileName) {
        String label = documentType != null ? documentType : "Document";
        RegistrantAuditLogHelper.persistForEntity(session, "Document", "UPLOAD",
                "Tải lên tài liệu " + label + (fileName != null ? ": " + fileName : ""),
                "Đã tải lên", profileId);
    }

    /** Ghi audit khi thí sinh gửi yêu cầu duyệt hồ sơ. */
    public static void logDocumentApprovalRequest(HttpSession session, int profileId, String note) {
        RegistrantAuditLogHelper.persistForEntity(session, "Document", "REQUEST",
                "Thí sinh gửi yêu cầu duyệt hồ sơ tài liệu",
                note != null && !note.isBlank() ? note.trim() : "Gửi duyệt", profileId);
    }

    /** Ghi audit khi thí sinh xóa một tài liệu đã tải. */
    public static void logDocumentDelete(HttpSession session, int profileId, String documentType, String fileName) {
        String label = documentType != null ? documentType : "Document";
        RegistrantAuditLogHelper.persistForEntity(session, "Document", "DELETE",
                "Xóa tài liệu " + label + (fileName != null && !fileName.isBlank() ? ": " + fileName : ""),
                "Đã xóa", profileId);
    }

    /** Ghi audit khi cập nhật hồ sơ cá nhân. */
    public static void logProfileUpdate(HttpSession session, int profileId, String summary) {
        RegistrantAuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                summary != null ? summary : "Cập nhật hồ sơ cá nhân",
                "Đã cập nhật", profileId);
    }

    /** Ghi audit khi tạo hồ sơ cá nhân lần đầu. */
    public static void logProfileCreate(HttpSession session, int profileId) {
        RegistrantAuditLogHelper.persistForEntity(session, "Profile", "INSERT",
                "Tạo hồ sơ cá nhân trên hệ thống", "Đã tạo", profileId);
    }

    /** Ghi audit khi gửi nguyện vọng ngày thi. */
    public static void logExamRegistration(HttpSession session, int profileId, String examLabel) {
        RegistrantAuditLogHelper.persistForEntity(session, "ExamRegistration", "INSERT",
                "Gửi nguyện vọng ngày thi: " + (examLabel != null ? examLabel : "-"),
                RegistrantExamSupport.PREFERRED_DATE_STATUS_LABEL, profileId);
    }

    /** Ghi audit khi đổi mật khẩu tài khoản. */
    public static void logPasswordChange(HttpSession session, int userId) {
        RegistrantAuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                "Đổi mật khẩu tài khoản", "Đã đổi mật khẩu", userId);
    }

    /** Ghi audit khi vô hiệu hoá tài khoản thí sinh. */
    public static void logAccountDeactivate(HttpSession session, int userId) {
        RegistrantAuditLogHelper.persistForEntity(session, "Profile", "UPDATE",
                "Vô hiệu hoá tài khoản thí sinh", "Đã vô hiệu hoá", userId);
    }

    /** Đổi danh sách AuditLog sang dòng theo dõi hồ sơ (timeline). */
    public static List<RegistrantTrackingLog> toTrackingLogs(List<AuditLogEntry> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return Collections.emptyList();
        }
        List<RegistrantTrackingLog> rows = new ArrayList<>(auditLogs.size());
        for (AuditLogEntry log : auditLogs) {
            rows.add(toTrackingLog(log));
        }
        return rows;
    }

    /** Đổi một bản ghi Audit thành một dòng timeline. */
    public static RegistrantTrackingLog toTrackingLog(AuditLogEntry log) {
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

    private static String buildEventTitle(AuditLogEntry log, String action) {
        String entity = AuditEntityLabels.toVietnamese(log.getTableName());
        return switch (action) {
            case "INSERT" -> "Thêm " + entity.toLowerCase(Locale.ROOT);
            case "UPDATE" -> "Cập nhật " + entity.toLowerCase(Locale.ROOT);
            case "DELETE" -> "Xóa " + entity.toLowerCase(Locale.ROOT);
            case "UPLOAD" -> "Tải lên " + entity.toLowerCase(Locale.ROOT);
            case "REQUEST" -> "Gửi duyệt " + entity.toLowerCase(Locale.ROOT);
            case "APPROVE" -> "Duyệt " + entity.toLowerCase(Locale.ROOT);
            case "REJECT" -> "Từ chối " + entity.toLowerCase(Locale.ROOT);
            default -> action + " " + entity;
        };
    }

    private static String mapStatusClass(String action) {
        return switch (action) {
            case "APPROVE", "INSERT", "UPLOAD" -> "approved";
            case "REJECT", "DELETE" -> "rejected";
            case "REQUEST" -> "pending";
            default -> "info";
        };
    }

    private static String mapStatusLabel(String action) {
        return switch (action) {
            case "APPROVE" -> "Đã duyệt";
            case "REJECT" -> "Từ chối";
            case "REQUEST" -> "Chờ duyệt";
            case "UPLOAD", "INSERT" -> "Thành công";
            case "DELETE" -> "Đã xóa";
            default -> "Đã cập nhật";
        };
    }

    private static String buildRemarks(AuditLogEntry log) {
        if (log.getDetails() != null && !log.getDetails().isBlank()) {
            return log.getDetails().trim();
        }
        if (log.getNewValue() != null && !log.getNewValue().isBlank()) {
            return log.getNewValue().trim();
        }
        return "-";
    }
}
