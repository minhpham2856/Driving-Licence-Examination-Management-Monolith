package service;

import enums.AuditAction;
import enums.AuditEntity;
import model.Audit;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message);

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId);

    void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId, String reason);

    List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId);

    String extractSbdForDisplay(Audit log, Map<Integer, String> sbdByRecordId);

    List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<Audit> getViolationLogsForSession(int sessionId, int limit);

    Map<Long, String> loadChangerNames(List<Audit> audits);
}
