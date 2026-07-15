package examstaff.service.impl.support.audit;

import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import examstaff.enums.AuditEntity;
import shared.model.Audit;

import java.sql.Timestamp;

/** Implementation: ghi audit log hành động cán bộ qua {@link AuditLogDAO}. */
public class StaffAuditLogServiceImpl {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    /**
     * Ghi một dòng audit cho thao tác của người dùng.
     *
     * @param userId   mã người dùng thực hiện
     * @param action   mã/loại hành động
     * @param details  mô tả chi tiết
     * @param recordId mã bản ghi liên quan (0 nếu không có)
     */
    public void logAction(int userId, String action, String details, int recordId) {
        try {
            // Mutate: dựng bản ghi Audit rồi insert
            Audit log = new Audit();
            log.setEntityName(AuditEntity.resolveLabel(resolveEntityName(action, details)));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(normalizeAction(action));
            log.setReason(details);
            log.setUserId(userId > 0 ? userId : 3);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            // Result
            auditLogDAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Map action/details thành tên entity audit hiển thị.
     *
     * @param action  mã hành động
     * @param details mô tả chi tiết
     * @return nhãn entity (Payment → Thanh toán nếu khớp)
     */
    static String resolveEntityName(String action, String details) {
        String resolved = examstaff.util.AuditLogHelper.resolveEntityName(action, details);
        if ("Payment".equalsIgnoreCase(resolved)) {
            return AuditEntity.THANH_TOAN.getDisplayName();
        }
        return resolved;
    }

    /**
     * Chuẩn hóa chuỗi action trước khi lưu.
     *
     * @param rawAct action thô
     * @return action đã chuẩn hoá
     */
    static String normalizeAction(String rawAct) {
        return examstaff.util.AuditLogHelper.normalizeAction(rawAct);
    }
}
