package registrant.dao;

import registrant.dto.AuditLogEntry;
import java.util.List;

/** Audit log queries used by the registrant portal. */
public interface AuditLogDAO {

    boolean insert(AuditLogEntry log);

    List<AuditLogEntry> getLogsByProfileId(int profileId, int limit);

    List<AuditLogEntry> getLogsByProfileIdFiltered(int profileId, int page, int pageSize,
            String searchQuery, String actionFilter, String fromDate, String toDate);

    int getLogsCountByProfileIdFiltered(int profileId, String searchQuery,
            String actionFilter, String fromDate, String toDate);

    List<String> listDistinctActionsByProfileId(int profileId);
}
