package service.impl;

import dao.AuditLogDAO;
import dao.impl.AuditLogDAOImpl;
import enums.AuditEntity;
import model.Audit;
import service.StaffAuditLogService;

import java.sql.Timestamp;
import java.util.List;

public class StaffAuditLogServiceImpl implements StaffAuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public void logAction(int userId, String action, String details) {
        logAction(userId, action, details, 0);
    }

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
        String resolved = util.AuditLogHelper.resolveEntityName(action, details);
        if ("Payment".equalsIgnoreCase(resolved)) {
            return AuditEntity.THANH_TOAN.getDisplayName();
        }
        return resolved;
    }

    static String normalizeAction(String rawAct) {
        return util.AuditLogHelper.normalizeAction(rawAct);
    }
}
