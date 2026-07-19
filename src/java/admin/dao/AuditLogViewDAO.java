package admin.dao;

import admin.model.AuditView;
import java.util.List;

public interface AuditLogViewDAO {
    List<AuditView> search(String keyword, String dbRole, String action,
                           String dateFrom, String dateTo, int page, int pageSize);
    int count(String keyword, String dbRole, String action, String dateFrom, String dateTo);
    int countAll();
    int countByAction(String action);
}
