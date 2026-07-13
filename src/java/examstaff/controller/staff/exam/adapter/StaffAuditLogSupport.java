package examstaff.controller.staff.exam.adapter;

import examstaff.util.SessionUserHelper;

import jakarta.servlet.http.HttpSession;
import examstaff.service.StaffAuditLogService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter ghi nhat ky exam staff; delegate StaffAuditLogService, khong goi DAO truc tiep.
 */
public final class StaffAuditLogSupport {

    private final StaffAuditLogService auditLogService;

    public StaffAuditLogSupport(StaffAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public void persist(HttpSession session, String action, String details, int recordId) {
        auditLogService.logAction(SessionUserHelper.resolveUserId(session), action, details, recordId);
    }

    public void persistWithSessionFeed(HttpSession session, String action, String details) {
        persistWithSessionFeed(session, action, details, 0);
    }

    public void persistWithSessionFeed(HttpSession session, String action, String details, int recordId) {
        appendExamFeed(session, action, details);
        persist(session, action, details, recordId);
    }

    @SuppressWarnings("unchecked")
    private static void appendExamFeed(HttpSession session, String action, String details) {
        if (session == null) {
            return;
        }
        List<Map<String, String>> examAuditLogs
                = (List<Map<String, String>>) session.getAttribute("examAuditLogs");
        if (examAuditLogs == null) {
            examAuditLogs = new ArrayList<>();
            session.setAttribute("examAuditLogs", examAuditLogs);
        }
        Map<String, String> audit = new HashMap<>();
        audit.put("time", new SimpleDateFormat("HH:mm").format(new Date()));
        audit.put("action", action);
        audit.put("details", details);
        examAuditLogs.add(0, audit);
    }
}
