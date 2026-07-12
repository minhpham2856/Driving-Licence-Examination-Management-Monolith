package examstaff.util;

import examstaff.service.StaffAuditLogService;
import jakarta.servlet.http.HttpSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StaffAuditLogSupport {

    private final StaffAuditLogService auditLogService;

    public StaffAuditLogSupport(StaffAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public void persist(HttpSession session, String action, String details, int recordId) {
        int userId = SessionUserHelper.resolveUserId(session);
        if (userId > 0) {
            auditLogService.logAction(userId, action, details, recordId);
        }
    }

    public void persistWithSessionFeed(HttpSession session, String action, String details) {
        persistWithSessionFeed(session, action, details, 0);
    }

    public void persistWithSessionFeed(HttpSession session, String action, String details, int recordId) {
        appendSessionFeed(session, action, details);
        persist(session, action, details, recordId);
    }

    @SuppressWarnings("unchecked")
    private static void appendSessionFeed(HttpSession session, String action, String details) {
        if (session == null) {
            return;
        }
        List<Map<String, String>> sessionAuditLogs
                = (List<Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        Map<String, String> audit = new HashMap<>();
        audit.put("time", new SimpleDateFormat("HH:mm").format(new Date()));
        audit.put("action", action);
        audit.put("details", details);
        sessionAuditLogs.add(0, audit);
    }
}
