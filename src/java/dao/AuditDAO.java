package dao;

import model.Audit;
import java.util.List;

public interface AuditDAO {
    List<Audit> getByUserId(int userId);
    int insert(Audit audit);
    List<Audit> findAll();
    List<Audit> getRecentLogs(int limit);
    List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);
    int getLogsCountForSession(int sessionId, String searchQuery);
    List<Audit> getViolationLogsForSession(int sessionId, int limit);
}
