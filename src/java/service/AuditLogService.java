package service;

import model.AuditRecordModel;

import java.util.List;
import java.util.Map;

public interface AuditLogService {

    void logAction(Integer userId, String action, String message);

    void logAction(Integer userId, String action, String message, int recordId);

    void logWarning(Integer userId, String message, String reason, int recordId);

    List<Map<String, Object>> toViewRows(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    Map<String, Object> toViewRow(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    String resolveSbd(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    List<AuditRecordModel> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<AuditRecordModel> getViolationLogsForSession(int sessionId, int limit);
}
