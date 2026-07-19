package registrant.dao;

import registrant.dto.AuditLogEntry;
import java.util.List;

/** Truy vấn bảng Audit cho cổng Registrant (ghi log + timeline theo dõi hồ sơ). */
public interface AuditLogDAO {

    /** Ghi một dòng audit. */
    boolean insert(AuditLogEntry log);

    /** Lấy tối đa {@code limit} bản ghi audit gần nhất của profile. */
    List<AuditLogEntry> getLogsByProfileId(int profileId, int limit);
}
