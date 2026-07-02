package util;

import enums.AuditEntityLabels;
import dao.AuditLogDAO;
import dao.impl.AuditLogDAOImpl;
import model.user.AuditLog;
import model.user.User;
import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.util.List;

public final class AuditLogHelper {

    private static final AuditLogDAO dao = new AuditLogDAOImpl();

    private AuditLogHelper() {
    }

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int recordId) {
        persistStatic(session, action, details, recordId);
    }

    /** Ghi audit với EntityName cố định (dùng cho cổng thí sinh / tài liệu). */
    public static void persistForEntity(HttpSession session, String entityName, String action,
            String details, String newValue, int recordId) {
        insertLog(session, action, details, null, newValue, null, null, recordId, entityName);
    }

    public static void persistStatic(HttpSession session, String action, String details, int recordId) {
        insertLog(session, action, details, null, details, null, null, recordId, null);
    }

    public static void persistChange(HttpSession session, String action, String details,
            String oldValue, String newValue, String reason, int recordId) {
        insertLog(session, action, details, oldValue, newValue, reason, null, recordId, null);
    }

    public static void persistFieldChanges(HttpSession session, String action, String contextDetails,
            List<AuditChangeDetails.FieldChange> changes, String reason, int recordId) {
        if (changes == null || changes.isEmpty()) {
            return;
        }
        for (AuditChangeDetails.FieldChange change : changes) {
            String detailsJson = AuditChangeDetails.toJson(List.of(change));
            insertLog(session, action, contextDetails, null, null, reason, detailsJson, recordId, null);
        }
    }

    private static void insertLog(HttpSession session, String action, String contextDetails,
            String oldValue, String newValue, String reason, String detailsJson, int recordId) {
        insertLog(session, action, contextDetails, oldValue, newValue, reason, detailsJson, recordId, null);
    }

    private static void insertLog(HttpSession session, String action, String contextDetails,
            String oldValue, String newValue, String reason, String detailsJson, int recordId,
            String explicitEntityName) {
        try {
            User user = (User) session.getAttribute("user");
            int userId = (user != null && user.getId() > 0) ? user.getId() : 3;

            AuditLog log = new AuditLog();
            String entity = explicitEntityName != null && !explicitEntityName.isBlank()
                    ? explicitEntityName
                    : resolveEntityName(action, contextDetails);
            log.setTableName(entity);
            log.setRecordId(recordId > 0 ? recordId : 0);
            log.setAction(normalizeAction(action));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setReason(reason);
            log.setDetails(detailsJson);
            log.setChangedBy(userId);
            log.setChangedAt(new Timestamp(System.currentTimeMillis()));
            dao.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void persistWarning(HttpSession session, String details, String reason, int recordId) {
        try {
            User user = (User) session.getAttribute("user");
            int userId = (user != null && user.getId() > 0) ? user.getId() : 3;

            AuditLog log = new AuditLog();
            log.setTableName("Candidate");
            log.setRecordId(recordId > 0 ? recordId : 0);
            log.setAction("WARNING");
            log.setNewValue(details);
            log.setReason(reason);
            log.setDetails(AuditChangeDetails.toJson(List.of(
                    new AuditChangeDetails.FieldChange("Trạng thái", "Hoạt động bình thường", "Đình chỉ"))));
            log.setChangedBy(userId);
            log.setChangedAt(new Timestamp(System.currentTimeMillis()));
            dao.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String resolveEntityName(String action, String details) {
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";

        if (upper.contains("DOCUMENT") || upper.contains(" on DOCUMENT")) {
            return "Document";
        }
        if (upper.contains("SCOREENTRY") || detailUpper.contains("HÀNG ĐỢI")) {
            return "ScoreEntryQueue";
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIẾT BỊ")) {
            return "ExamDevice";
        }
        if (upper.contains("IMPORT")) {
            return "ExamRegistration";
        }
        if (upper.contains("PAYMENT")) {
            return "Payment";
        }
        if (upper.contains("PERSON") || upper.contains("PROFILE")) {
            return "Profile";
        }
        if (upper.contains("EXAMINER") || upper.contains("ASSIGN") || upper.contains("REMOVE")) {
            return "Session_Examiner";
        }
        if (detailUpper.contains("ĐIỂM") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÝ THUYẾT")
                || detailUpper.contains("THỰC HÀNH") || detailUpper.contains("ĐƯỜNG TRƯỜNG")) {
            return "ExamScore";
        }
        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {
            return "ExamRegistration";
        }
        if (upper.contains("SESSION")) {
            return "Session";
        }
        return "Candidate";
    }

    static String normalizeAction(String rawAct) {
        if (rawAct == null) {
            return "UPDATE";
        }
        String upper = rawAct.toUpperCase();
        if (upper.contains("IMPORT")) {
            return "IMPORT";
        }
        if (upper.contains("INSERT")) {
            return "INSERT";
        }
        if (upper.contains("DELETE") || upper.contains("REMOVE")) {
            return "DELETE";
        }
        if (upper.contains("EXPORT")) {
            return "EXPORT";
        }
        if (upper.contains("ASSIGN")) {
            return "ASSIGN";
        }
        if (upper.contains("REQUEST")) {
            return "REQUEST";
        }
        if (upper.contains("APPROVE")) {
            return "APPROVE";
        }
        if (upper.contains("REJECT")) {
            return "REJECT";
        }
        if (upper.contains("UPLOAD")) {
            return "UPLOAD";
        }
        return "UPDATE";
    }
}
