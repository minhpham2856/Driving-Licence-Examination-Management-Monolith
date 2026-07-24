package registrant.util;

import auth.dto.UserDTO;
import registrant.dao.AuditLogDAO;
import registrant.dao.impl.AuditLogDAOImpl;
import registrant.dto.AuditLogEntry;
import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import shared.Attributes;

/**
 * Lớp tiện ích ghi audit log cho cổng thí sinh — tầng util gọi {@link registrant.dao.AuditLogDAO}.
 * <p>
 * Lấy {@code UserId} từ session, persist vào bảng {@code Audit} với {@code EntityName}, {@code Action},
 * {@code Details} và {@code NewValue}. Được gọi từ {@link registrant.util.RegistrantAuditHelper} và các service.
 */
public final class RegistrantAuditLogHelper {

    private static final AuditLogDAO DAO = new AuditLogDAOImpl();

    private RegistrantAuditLogHelper() {
    }

    /** Persist audit cho entity/action từ session thí sinh hiện tại. */
    public static void persistForEntity(HttpSession session, String entityName, String action,
            String details, String newValue, int recordId) {
        insertLog(session, action, details, null, newValue, null, null, recordId, entityName);
    }

    private static void insertLog(HttpSession session, String action, String contextDetails,
            String oldValue, String newValue, String reason, String detailsJson, int recordId,
            String explicitEntityName) {
        try {
            UserDTO user = session != null
                    ? (UserDTO) session.getAttribute(shared.Attributes.Session.USER) : null;
            int userId = user != null && user.getUserId() > 0 ? user.getUserId() : 3;

            AuditLogEntry log = new AuditLogEntry();
            String entity = explicitEntityName != null && !explicitEntityName.isBlank()
                    ? explicitEntityName : "Profile";
            log.setTableName(entity);
            log.setRecordId(recordId > 0 ? recordId : 0);
            log.setAction(action != null ? action : "UPDATE");
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setReason(reason);
            // Ưu tiên mô tả nghiệp vụ (contextDetails); detailsJson chỉ khi gọi kèm JSON
            String details = contextDetails != null && !contextDetails.isBlank()
                    ? contextDetails.trim()
                    : detailsJson;
            log.setDetails(details);
            log.setChangedBy(userId);
            log.setChangedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception ignored) {
            // Audit failure must not block user flow.
        }
    }
}
