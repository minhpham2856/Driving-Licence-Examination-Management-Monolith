package Utils;

import jakarta.servlet.http.HttpSession;

/**
 * Ghi nhật ký Audit cho các thao tác cổng thí sinh — entity và action thống nhất với bảng Audit.
 */
public final class RegistrantAuditHelper {

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
}
