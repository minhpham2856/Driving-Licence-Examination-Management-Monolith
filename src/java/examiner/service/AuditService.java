package examiner.service;

import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.model.Audit;

import java.util.List;
import java.util.Map;

// Service contract for writing audit entries and transforming logs for examiner/admin views.
public interface AuditService {

    // Writes a simple audit log entry without record id or reason.
    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message);

    // Writes an audit log entry tied to a specific record id.
    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId);

    // Writes an audit log entry with record id and optional reason text.
    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId, String reason);

    // Converts one audit row into one or more JSP-friendly view row maps.
    List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    // Converts one audit row into a single JSP-friendly view row map.
    Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    // Extracts candidate number (SBD) text from audit fields for display.
    String extractSbdForDisplay(Audit log, Map<Integer, String> sbdByRecordId);

    // Loads paginated audit logs for an exam with optional search filter.
    List<Audit> getAllByExam(int examId, int page, int pageSize, String searchQuery);

    // Returns total audit log count for an exam matching the search filter.
    int countAllByExam(int examId, String searchQuery);

    // Loads recent violation-related audit logs for an exam up to a limit.
    List<Audit> getAllViolationsByExam(int examId, int limit);

    // Resolves display names for users who performed audited actions.
    Map<Long, String> getAllChangerNamesByAudit(List<Audit> audits);

    // Searches audit logs by keyword and maps them to admin-style view rows.
    List<Map<String, Object>> getFiltered(String keyword, int limit);

    // Personal audit logs for one user, optionally restricted to a single day.
    List<Audit> getAllByUser(int userId, String dateFilter);
}
