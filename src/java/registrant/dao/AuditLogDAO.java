package registrant.dao;

import registrant.dto.AuditLogEntry;
import java.util.List;

/**
 * Hợp đồng truy vấn và ghi bảng {@code Audit} cho cổng thí sinh.
 * <p>
 * {@link #insert} persist hành động upload/profile/…; {@link #getLogsByProfileId} lấy nhật ký
 * phục vụ timeline {@code track-profile.jsp} và {@link registrant.util.RegistrantAuditHelper}.
 */
public interface AuditLogDAO {

    /** Ghi một dòng audit. */
    boolean insert(AuditLogEntry log);

    /** Lấy tối đa {@code limit} bản ghi audit gần nhất của profile. */
    List<AuditLogEntry> getLogsByProfileId(int profileId, int limit);
}
