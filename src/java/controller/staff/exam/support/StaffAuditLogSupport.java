package controller.staff.exam.support;

import jakarta.servlet.http.HttpSession;
import service.StaffAuditLogService;
import service.impl.StaffAuditLogServiceImpl;
import util.SessionUserHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter ghi nhật ký exam staff — delegate {@link StaffAuditLogService}, không gọi DAO trực tiếp.
 */
public final class StaffAuditLogSupport {

    private static final StaffAuditLogService AUDIT_LOG = new StaffAuditLogServiceImpl();

    private StaffAuditLogSupport() {
    }

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int recordId) {
        AUDIT_LOG.logAction(SessionUserHelper.resolveUserId(session), action, details, recordId);
    }

    public static void persistWithSessionFeed(HttpSession session, String action, String details) {
        persistWithSessionFeed(session, action, details, 0);
    }

    public static void persistWithSessionFeed(HttpSession session, String action, String details, int recordId) {
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
