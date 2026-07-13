package examiner.service;

import examiner.enums.AuditAction;
import examiner.enums.AuditEntity;
import shared.model.Audit;

import java.util.List;
import java.util.Map;

public interface AuditService {

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message);

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId);

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId, String reason);

    List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    String extractSbdForDisplay(Audit log, Map<Integer, String> sbdByRecordId);

    List<Audit> getLogsForExamPaginated(int examId, int page, int pageSize, String searchQuery);

    int getLogsCountForExam(int examId, String searchQuery);

    List<Audit> getViolationLogsForExam(int examId, int limit);

    Map<Long, String> loadChangerNames(List<Audit> audits);

    List<Map<String, Object>> searchLogs(String keyword, int limit);

    // Personal audit logs for one user, optionally restricted to a single day.
    List<Audit> getLogsByUser(int userId, String dateFilter);
}

