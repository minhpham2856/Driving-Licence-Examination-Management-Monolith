package registrant.dao;

import registrant.dto.AuditLogEntry;
import java.util.List;

/**
 * Hợp đồng truy vấn và ghi bảng Audit cho cổng thí sinh.
 * insert persist hành động upload/profile/…; getLogsByProfileId lấy nhật ký phục vụ timeline track-profile.jsp và RegistrantAuditHelper.
 */
public interface AuditLogDAO {

    /** Ghi một dòng audit. */
    boolean insert(AuditLogEntry log);

    /** Lấy tối đa limit bản ghi audit gần nhất của profile. */
    List<AuditLogEntry> getLogsByProfileId(int profileId, int limit);
}
