package service;
import model.Audit;
import java.util.List;
import java.util.Map;
public interface AuditLogService {
    void logAction(Integer userId, String action, String message);
    void logAction(Integer userId, String action, String message, int recordId);
    void logWarning(Integer userId, String message, String reason, int recordId);
    List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId);
    Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId);
    String resolveSbd(Audit log, Map<Integer, String> sbdByRecordId);
    List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);
    int getLogsCountForSession(int sessionId, String searchQuery);
    List<Audit> getViolationLogsForSession(int sessionId, int limit);
    Map<Long, String> loadChangerNames(List<Audit> audits);
}
