package examstaff.service.impl;

import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import examstaff.enums.AuditEntity;
import shared.model.Audit;
import examstaff.service.StaffAuditLogService;

import java.sql.Timestamp;

/** Implementation: ghi audit log hành động cán bộ qua {@link AuditLogDAO}. */
public class StaffAuditLogServiceImpl implements StaffAuditLogService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    /**
     * Ghi một dòng audit cho thao tác của người dùng.
     *
     * @param userId   mã người dùng thực hiện
     * @param action   mã/loại hành động
     * @param details  mô tả chi tiết
     * @param recordId mã bản ghi liên quan (0 nếu không có)
     */
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

    /** Map action/details thành tên entity audit hiển thị. */
    static String resolveEntityName(String action, String details) {
        String resolved = examstaff.util.AuditLogHelper.resolveEntityName(action, details);
        if ("Payment".equalsIgnoreCase(resolved)) {
            return AuditEntity.THANH_TOAN.getDisplayName();
        }
        return resolved;
    }

    /** Chuẩn hóa chuỗi action trước khi lưu. */
    static String normalizeAction(String rawAct) {
        return examstaff.util.AuditLogHelper.normalizeAction(rawAct);
    }
}
