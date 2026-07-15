package managingstaff.dao;

import java.util.List;
import managingstaff.dto.AuditDTO;

public interface AuditLogDAO {
    boolean insert(int userId, String action, String entityName, String entityId,
            String oldValue, String newValue, String reason, String details);
    List<AuditDTO> getLogsByUserAndDate(int userId, String date);
    List<AuditDTO> searchUserLogsPaginated(int userId, String keyword, String action,
            String startDate, String endDate, int page, int pageSize);
    int countUserLogs(int userId, String keyword, String action,
            String startDate, String endDate);
}
