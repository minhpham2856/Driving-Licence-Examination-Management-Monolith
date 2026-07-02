package service.impl;

import dao.AuditDAO;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import model.Audit;
import model.Profile;
import model.User;
import service.AuditLogService;
import enums.AuditAction;
import enums.AuditEntity;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuditLogServiceImpl implements AuditLogService {

    private final AuditDAO DAO = new AuditDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private static final Pattern SBD_PATTERN = Pattern.compile("SBD\\s+([A-Za-z0-9]+-\\d+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Override
    public void logAction(Integer userId, String action, String message) {
        logAction(userId, action, message, 0);
    }

    @Override
    public void logAction(Integer userId, String action, String message, int recordId) {
        insertLog(userId, action, message, null, message, null, recordId);
    }

    @Override
    public void logWarning(Integer userId, String message, String reason, int recordId) {
        insertLog(userId, AuditAction.CANH_BAO.name(), message, null, message, reason, recordId);
    }

    private void insertLog(Integer actionUserId, String action, String contextDetails,
            String oldValue, String newValue, String reason, int recordId) {
        try {
            int userId = (actionUserId != null && actionUserId > 0) ? actionUserId : 3;
            Audit log = new Audit();
            log.setEntityName(AuditEntity.resolveLabel(resolveEntityName(action, contextDetails)));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(normalizeAction(action));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setReason(reason);
            log.setUserId(userId);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolveEntityName(String action, String details) {
        String upper = action != null ? action.toUpperCase() : "";
        String detailUpper = details != null ? details.toUpperCase() : "";
        if (upper.contains("SCOREENTRY") || detailUpper.contains("HẠNG ĐIỂM")) {
            return "ScoreEntryQueue";
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIẾT BỊ")) {
            return "ExamDevice";
        }
        if (upper.contains("IMPORT")) {
            return "ExamRegistration";
        }
        if (upper.contains("PAYMENT")) {
            return "Payment";
        }
        if (upper.contains("PERSON") || upper.contains("PROFILE")) {
            return "Profile";
        }
        if (upper.contains("EXAMINER") || upper.contains("ASSIGN") || upper.contains("REMOVE")) {
            return "ExaminerSchedule";
        }
        if (detailUpper.contains("ĐIỂM") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÝ THUYẾT")
                || detailUpper.contains("THỰC HÀNH") || detailUpper.contains("ĐƯỜNG TRƯỜNG")) {
            return "ExamScore";
        }
        if (upper.contains("EXAMREGISTRATION") || upper.contains("ALLOCATE")) {
            return "Candidate";
        }
        if (upper.contains("SESSION")) {
            return "Session";
        }
        return "Candidate";
    }

    private String normalizeAction(String rawAct) {
        return AuditAction.normalize(rawAct).getDisplayName();
    }

    @Override
    public List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        return List.of(toViewRow(log, changerName, sbdByRecordId));
    }

    @Override
    public Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String message = firstNonBlank(log.getNewValue(), log.getDetails());
        String reason = normalizeReason(log);
        row.put("username", nullToDash(changerName));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", AuditEntity.resolveLabel(log.getEntityName()));
        row.put("sbd", sbd);
        row.put("newValueClass", mapNewValueClass(action));
        row.put("multiline", message != null && message.contains("\n"));
        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            row.put("info", buildChangeInfo(log, sbd));
            row.put("oldValue", log.getOldValue());
            row.put("newValue", nullToDash(message));
            row.put("reason", nullToDash(reason));
        } else {
            row.put("info", message != null && !message.isBlank() ? message : buildChangeInfo(log, sbd));
            row.put("oldValue", null);
            row.put("newValue", nullToDash(message));
            row.put("reason", nullToDash(reason));
        }
        return row;
    }

    @Override
    public String resolveSbd(Audit log, Map<Integer, String> sbdByRecordId) {
        for (String text : new String[]{log.getNewValue(), log.getOldValue(), log.getReason(), log.getDetails()}) {
            String extracted = extractSbdFromText(text);
            if (extracted != null) {
                return extracted;
            }
        }
        Integer recordId = parseEntityId(log.getEntityId());
        if (recordId != null && recordId > 0 && sbdByRecordId != null) {
            String mapped = sbdByRecordId.get(recordId);
            if (mapped != null && !mapped.isBlank()) {
                return mapped;
            }
        }
        return "-";
    }

    private static Integer parseEntityId(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(entityId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractSbdFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = SBD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String normalizeReason(Audit log) {
        String reason = log.getReason();
        if (reason == null || reason.isBlank()) {
            return null;
        }
        if (log.getOldValue() == null || log.getOldValue().isBlank()) {
            if (log.getNewValue() != null && reason.equals(log.getNewValue())) {
                return null;
            }
        }
        return reason;
    }

    private String buildChangeInfo(Audit log, String sbd) {
        String entity = AuditEntity.resolveLabel(log.getEntityName());
        String action = log.getAction() != null ? log.getAction() : AuditAction.CAP_NHAT.getDisplayName();
        String sbdSuffix = "-".equals(sbd) ? "" : " SBD " + sbd;
        return switch (action) {
            case "Cảnh báo" ->
                "Cảnh báo" + sbdSuffix;
            case "Thêm" ->
                "Thêm " + entity.toLowerCase() + sbdSuffix;
            case "Xóa" ->
                "Xóa " + entity.toLowerCase() + sbdSuffix;
            default ->
                "Cập nhật " + entity.toLowerCase() + sbdSuffix;
        };
    }

    private String mapActionLabel(String action) {
        return AuditAction.normalize(action).getDisplayName();
    }

    private String mapActionBadge(String action) {
        AuditAction normalized = AuditAction.normalize(action);
        return switch (normalized) {
            case THEM ->
                "audit-badge--insert";
            case XOA ->
                "audit-badge--delete";
            case XUAT ->
                "audit-badge--export";
            case PHAN_CONG ->
                "audit-badge--assign";
            case NHAP ->
                "audit-badge--import";
            case CANH_BAO ->
                "audit-badge--warning";
            case HE_THONG ->
                "audit-badge--system";
            case DUYET ->
                "audit-badge--approve";
            default ->
                "audit-badge--update";
        };
    }

    private String mapNewValueClass(String action) {
        if (AuditAction.normalize(action) == AuditAction.XOA) {
            return "audit-td--old";
        }
        return "audit-td--new";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        return DAO.getLogsForSessionPaginated(sessionId, page, pageSize, searchQuery);
    }

    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        return DAO.getLogsCountForSession(sessionId, searchQuery);
    }

    @Override
    public List<Audit> getViolationLogsForSession(int sessionId, int limit) {
        return DAO.getViolationLogsForSession(sessionId, limit);
    }

    @Override
    public Map<Long, String> loadChangerNames(List<Audit> audits) {
        Map<Long, String> names = new LinkedHashMap<>();
        if (audits == null || audits.isEmpty()) {
            return names;
        }
        List<Integer> userIds = audits.stream()
                .map(Audit::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userDAO.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        Map<Integer, Profile> profileMap = profileDAO.getAllByUserIds(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));
        for (Audit audit : audits) {
            Integer userId = audit.getUserId();
            User user = userId == null ? null : userMap.get(userId);
            Profile profile = userId == null ? null : profileMap.get(userId);
            if (user != null && user.getUsername() != null) {
                names.put(audit.getAuditId(), user.getUsername());
            } else if (profile != null && profile.getFullName() != null) {
                names.put(audit.getAuditId(), profile.getFullName());
            } else {
                names.put(audit.getAuditId(), "Unknown");
            }
        }
        return names;
    }
}
