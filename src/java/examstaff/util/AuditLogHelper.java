package examstaff.util;




import examstaff.dao.AuditLogDAO;

import examstaff.dao.impl.AuditLogDAOImpl;

import shared.model.Audit;
import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.util.List;

public final class AuditLogHelper {

    private static final AuditLogDAO DAO = new AuditLogDAOImpl();

    private AuditLogHelper() {
    }

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int recordId) {
        persistStatic(session, action, details, recordId);
    }

    public static void persistStatic(HttpSession session, String action, String details, int recordId) {
        insertLog(session, action, details, null, details, null, null, recordId);
    }

    public static void persistChange(HttpSession session, String action, String details,
            String oldValue, String newValue, String reason, int recordId) {
        insertLog(session, action, details, oldValue, newValue, reason, null, recordId);
    }

    public static void persistFieldChanges(HttpSession session, String action, String contextDetails,
            List<AuditChangeDetails.FieldChange> changes, String reason, int recordId) {
        if (changes == null || changes.isEmpty()) {
            return;
        }
        for (AuditChangeDetails.FieldChange change : changes) {
            String detailsJson = AuditChangeDetails.toJson(List.of(change));
            insertLog(session, action, contextDetails, null, null, reason, detailsJson, recordId);
        }
    }

    private static void insertLog(HttpSession session, String action, String contextDetails,
            String oldValue, String newValue, String reason, String detailsJson, int recordId) {
        try {
            int userId = SessionUserHelper.resolveUserId(session);

            Audit log = new Audit();
            log.setEntityName(examstaff.enums.AuditEntity.resolveLabel(resolveEntityName(action, contextDetails)));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(normalizeAction(action));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            String reasonText = reason;
            if ((reasonText == null || reasonText.isBlank()) && detailsJson != null && !detailsJson.isBlank()) {
                reasonText = detailsJson;
            } else if (reasonText != null && detailsJson != null && !detailsJson.isBlank()) {
                reasonText = reasonText + " | " + detailsJson;
            }
            if ((reasonText == null || reasonText.isBlank()) && contextDetails != null && !contextDetails.isBlank()) {
                reasonText = contextDetails;
            }
            log.setReason(reasonText);
            log.setDetails(detailsJson);
            log.setUserId(userId);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void persistWarning(HttpSession session, String details, String reason, int recordId) {
        try {
            int userId = SessionUserHelper.resolveUserId(session);

            Audit log = new Audit();
            log.setEntityName(examstaff.enums.AuditEntity.resolveLabel("Candidate"));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction("WARNING");
            log.setNewValue(details);
            log.setReason(reason);
            log.setDetails(AuditChangeDetails.toJson(List.of(
                    new AuditChangeDetails.FieldChange("Tráº¡ng thÃ¡i", "Hoáº¡t Ä‘á»™ng bÃ¬nh thÆ°á»ng", "ÄÃ¬nh chá»‰"))));
            log.setUserId(userId);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String resolveEntityName(String action, String details) {
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";

        if (upper.contains("SCOREENTRY") || detailUpper.contains("HÃ€NG Äá»¢I")) {
            return "ScoreEntryQueue";
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIáº¾T Bá»Š")) {
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
            return "ExaminerSchedule";
        }
        if (detailUpper.contains("ÄIá»‚M") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÃ THUYáº¾T")
                || detailUpper.contains("THá»°C HÃ€NH") || detailUpper.contains("ÄÆ¯á»œNG TRÆ¯á»œNG")) {
            return "ExamScore";
        }
        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {
            return "Candidate";
        }
        if (upper.contains("SESSION")) {
            return "Session";
        }
        return "Candidate";
    }

    public static String normalizeAction(String rawAct) {
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
        return "UPDATE";
    }
}



