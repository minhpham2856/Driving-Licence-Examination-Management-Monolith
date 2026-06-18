package DAO;

import Models.AuditLog;
import Models.StaffProcedureKpi;
import java.util.List;

public interface AuditLogDAO {
    boolean insert(AuditLog log);
    List<AuditLog> getLogsByUserToday(int userId);
    List<AuditLog> getAllLogsToday();
    
    // New methods for date filtering and past logs
    List<AuditLog> getLogsByUserAndDate(int userId, String dateStr);
    List<AuditLog> getAllLogsByDate(String dateStr);
    
    // Paginated methods
    List<AuditLog> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);
    List<AuditLog> getAllLogsByDatePaginated(String dateStr, int page, int pageSize);
    
    int getLogsCountByUserAndDate(int userId, String dateStr);
    int getAllLogsCountByDate(String dateStr);

    /** Học viên đã ảnh + thanh toán, do cán bộ userId thu (qua Audit). filterDate: yyyy-MM-dd hoặc null. */
    StaffProcedureKpi getStaffProcedureKpi(int userId, String filterDate);

    List<AuditLog> getLogsForSessionPaginated(int sessionId, int page, int pageSize);

    int getLogsCountForSession(int sessionId);

    List<AuditLog> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<AuditLog> getViolationLogsForSession(int sessionId, int limit);
}
