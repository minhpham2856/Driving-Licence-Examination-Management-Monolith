package examiner.dao;

import shared.model.Audit;
import java.util.List;

public interface AuditDAO {

    int insert(Audit audit);

    List<Audit> getRecentLogs(int limit);

    List<Audit> getLogsForExamPaginated(int examId, int page, int pageSize, String searchQuery);

    int getLogsCountForExam(int examId, String searchQuery);

    List<Audit> getViolationLogsForExam(int examId, int limit);

    List<Audit> searchAll(String keyword, int limit);

    // Personal audit logs for one staff member, optionally filtered to a single
    // day (dateFilter is a yyyy-MM-dd string; null/blank means all history).
    List<Audit> getLogsByUser(int userId, String dateFilter);
}

