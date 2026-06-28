package service;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import util.AuditChangeDetails;
import model.user.AuditRecordModel;

public interface AuditLogService {

    void persist(HttpSession session, String action, String details);

    void persist(HttpSession session, String action, String details, int recordId);

    void persistChange(HttpSession session, String action, String details,
            String oldValue, String newValue, String reason, int recordId);

    void persistFieldChanges(HttpSession session, String action, String contextDetails,
            List<AuditChangeDetails.FieldChange> changes, String reason, int recordId);

    void persistWarning(HttpSession session, String details, String reason, int recordId);

    List<Map<String, Object>> toViewRows(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    Map<String, Object> toViewRow(AuditRecordModel log, Map<Integer, String> sbdByRecordId);
    
    String resolveSbd(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    List<AuditRecordModel> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<AuditRecordModel> getViolationLogsForSession(int sessionId, int limit);
}
