package examstaff.service.impl;

import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import examstaff.enums.AuditEntity;
import examstaff.model.Audit;
import examstaff.service.StaffAuditLogService;

import java.sql.Timestamp;

public class StaffAuditLogServiceImpl implements StaffAuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public void logAction(int userId, String action, String details, int recordId) {
        try {
            Audit log = new Audit();
            log.setEntityName(AuditEntity.resolveLabel(resolveEntityName(action, details)));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(normalizeAction(action));
            log.setReason(details);
            log.setUserId(userId > 0 ? userId : 3);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            auditLogDAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String resolveEntityName(String action, String details) {
        String resolved = examstaff.util.AuditLogHelper.resolveEntityName(action, details);
        if ("Payment".equalsIgnoreCase(resolved)) {
            return AuditEntity.THANH_TOAN.getDisplayName();
        }
        return resolved;
    }

    static String normalizeAction(String rawAct) {
        return examstaff.util.AuditLogHelper.normalizeAction(rawAct);
    }
}
