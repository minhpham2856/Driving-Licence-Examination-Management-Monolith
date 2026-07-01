package DAO;

import Models.AuditView;
import java.util.List;

/**
 * Read-only audit viewer for the Admin screen. Distinct name so it does NOT
 * clash with the team's DAO.AuditLogDAO. Supports keyword / role / action /
 * date-range filtering + pagination, which the team DAO does not.
 */
public interface AuditLogViewDAO {
    List<AuditView> search(String keyword, String dbRole, String action,
                           String dateFrom, String dateTo, int page, int pageSize);
    int count(String keyword, String dbRole, String action, String dateFrom, String dateTo);
    int countAll();
    int countByAction(String action);
}
