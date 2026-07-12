package examstaff.service.impl;

import examstaff.dao.AuditLogDAO;
import examstaff.dao.impl.AuditLogDAOImpl;
import shared.enums.AuditEntity;
import shared.model.Audit;
import examstaff.service.StaffAuditLogService;

import java.sql.Timestamp;

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
            log.setEntityName(resolveEntityName(action, details));
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
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";

        if (upper.contains("SCOREENTRY") || detailUpper.contains("HÀNG ĐỢI")) {
            return AuditEntity.EXAM_SCORE.getValue();
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIẾT BỊ")) {
            return AuditEntity.EXAM_DEVICE.getValue();
        }
        if (upper.contains("IMPORT")) {
            return AuditEntity.DOSSIER.getValue();
        }
        if (upper.contains("PAYMENT")) {
            return AuditEntity.PAYMENT.getValue();
        }
        if (upper.contains("PERSON") || upper.contains("PROFILE")) {
            return AuditEntity.CANDIDATE.getValue();
        }
        if (upper.contains("EXAMINER") || upper.contains("ASSIGN") || upper.contains("REMOVE")) {
            return AuditEntity.EXAMINER_ASSIGNMENT.getValue();
        }
        if (detailUpper.contains("ĐIỂM") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÝ THUYẾT")
                || detailUpper.contains("THỰC HÀNH") || detailUpper.contains("ĐƯỜNG TRƯỜNG")) {
            return AuditEntity.EXAM_SCORE.getValue();
        }
        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {
            return AuditEntity.CANDIDATE.getValue();
        }
        if (upper.contains("SESSION")) {
            return AuditEntity.EXAM_SESSION.getValue();
        }
        return AuditEntity.CANDIDATE.getValue();
    }

    static String normalizeAction(String rawAct) {
        if (rawAct == null) {
            return "UPDATE";
        }
        String upper = rawAct.toUpperCase();
        if (upper.contains("IMPORT")) {
            return "IMPORT";
        }
        if (upper.contains("INSERT")) {
            return "INSERT";
        }
        if (upper.contains("DELETE") || upper.contains("REMOVE")) {
            return "DELETE";
        }
        if (upper.contains("EXPORT")) {
            return "EXPORT";
        }
        if (upper.contains("ASSIGN")) {
            return "ASSIGN";
        }
        return "UPDATE";
    }
}
