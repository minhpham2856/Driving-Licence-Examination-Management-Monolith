package managingstaff.util;

import jakarta.servlet.http.HttpSession;
import managingstaff.dao.AuditLogDAO;
import managingstaff.dao.impl.AuditLogDAOImpl;

public final class AuditLogHelper {
    private AuditLogHelper() { }

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int entityId) {
        new AuditLogDAOImpl().insert(SessionUtil.currentUserId(session), action,
                entityName(action), String.valueOf(Math.max(entityId, 0)),
                null, details, details, details);
    }

    public static void persistChange(HttpSession session, String action, String details,
            String oldValue, String newValue, String entityName, int entityId) {
        AuditLogDAO dao = new AuditLogDAOImpl();
        dao.insert(SessionUtil.currentUserId(session), action, entityName,
                String.valueOf(entityId), oldValue, newValue, details, details);
    }

    private static String entityName(String action) {
        String upper = action == null ? "" : action.toUpperCase();
        if (upper.contains("DOSSIER")) return "ExamRegistration";
        if (upper.contains("SESSION") || upper.contains("EXAM")) return "Exam";
        if (upper.contains("USER")) return "User";
        return "ManagingStaff";
    }
}
