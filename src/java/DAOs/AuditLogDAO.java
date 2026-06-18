package DAOs;

import Models.Audit;
import DTOs.AuditDTO;
import DTOs.StaffProcedureKpi;
import java.util.List;

public interface AuditLogDAO {
    boolean insert(Audit log);
    List<AuditDTO> getLogsByUserToday(int userId);
    List<AuditDTO> getAllLogsToday();
    
    // New methods for date filtering and past logs
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);
    List<AuditDTO> getAllLogsByDate(String dateStr);
    
    // Paginated methods
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);
    List<AuditDTO> getAllLogsByDatePaginated(String dateStr, int page, int pageSize);
    
    int getLogsCountByUserAndDate(int userId, String dateStr);
    int getAllLogsCountByDate(String dateStr);

    /** Học viên đã ảnh + thanh toán, do cán bộ userId thu (qua Audit). filterDate: yyyy-MM-dd hoặc null. */
    StaffProcedureKpi getStaffProcedureKpi(int userId, String filterDate);

    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize);

    int getLogsCountForSession(int sessionId);

    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    int getLogsCountForSession(int sessionId, String searchQuery);

    List<AuditDTO> getViolationLogsForSession(int sessionId, int limit);
}
