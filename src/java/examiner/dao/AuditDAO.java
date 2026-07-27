package examiner.dao;

import shared.model.Audit;
import java.util.List;

// DAO contract for Audit persistence; examiner module SQL boundary.
public interface AuditDAO {

    // Inserts an audit log row and returns generated AuditId.
    int add(Audit audit);

    // Returns the most recent audit rows across the system.
    List<Audit> getRecentLogs(int limit);

    // Returns paginated audit rows scoped to one exam with optional text search.
    List<Audit> getAllByExam(int examId, int page, int pageSize, String searchQuery);

    // Returns total audit row count for one exam with optional text search.
    int countAllByExam(int examId, String searchQuery);

    // Returns recent violation-related audit rows for one exam.
    List<Audit> getAllViolationsByExam(int examId, int limit);

    // Searches audit rows by keyword across all entities.
    List<Audit> getFiltered(String keyword, int limit);

    // Personal audit logs for one staff member, optionally filtered to a single day (dateFilter is yyyy-MM-dd; null/blank means all history).
    List<Audit> getAllByUser(int userId, String dateFilter);
}
