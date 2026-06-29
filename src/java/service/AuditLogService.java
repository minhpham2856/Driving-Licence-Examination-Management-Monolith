package service;

import model.*;
import java.util.List;
import java.util.Map;
import util.AuditChangeDetails;
import model.AuditRecordModel;

public interface AuditLogService {

    void persist(Integer actionUserId, String action, String details);

    void persist(Integer actionUserId, String action, String details, int recordId);
    boolean insertAudit(Audit audit);

    void persistChange(Integer actionUserId, String action, String details,
            String oldValue, String newValue, String reason, int recordId);

    void persistFieldChanges(Integer actionUserId, String action, String contextDetails,
            List<AuditChangeDetails.FieldChange> changes, String reason, int recordId);

    void persistWarning(Integer actionUserId, String details, String reason, int recordId);

    List<Map<String, Object>> toViewRows(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    Map<String, Object> toViewRow(AuditRecordModel log, Map<Integer, String> sbdByRecordId);
    
    String resolveSbd(AuditRecordModel log, Map<Integer, String> sbdByRecordId);

    List<AuditRecordModel> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<AuditRecordModel> getViolationLogsForSession(int sessionId, int limit);
}
