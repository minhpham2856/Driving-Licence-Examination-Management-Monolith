package service.impl;

import dao.AuditDAO;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import enums.AuditAction;
import enums.AuditEntity;
import model.Audit;
import model.Profile;
import model.User;
import service.AuditLogService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    private static final SimpleDateFormat AUDIT_TIME_FMT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message) {
        logAction(userId, action, entity, message, 0);
    }

    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId) {
        logAction(userId, action, entity, message, recordId, null);
    }

    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message,
            int recordId, String reason) {
        insertLog(userId, action, entity, message, recordId, reason);
    }

    private void insertLog(Integer actionUserId, AuditAction action, AuditEntity entity, String details,
            int recordId, String reason) {
        try {
            int userId = (actionUserId != null && actionUserId > 0) ? actionUserId : 3;
            Audit log = new Audit();
            log.setEntityName(entity.getValue());
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(action.getValue());
            log.setOldValue(null);
            log.setNewValue(details);
            log.setReason(reason);
            log.setUserId(userId);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        return List.of(toViewRow(log, changerName, sbdByRecordId));
    }

    @Override
    public Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        AuditAction action = actionFromDb(log.getAction());
        String sbd = resolveSbd(log, sbdByRecordId);
        String message = firstNonBlank(log.getNewValue(), log.getDetails());
        String reason = normalizeReason(log);
        row.put("username", nullToDash(changerName));
        row.put("actionLabel", action.getValue());
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", entityLabelFromDb(log.getEntityName()));
        row.put("sbd", sbd);
        row.put("newValueClass", action == AuditAction.DELETE ? "audit-td--old" : "audit-td--new");
        row.put("multiline", message != null && message.contains("\n"));
        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            row.put("info", buildChangeInfo(log, action, sbd));
            row.put("oldValue", log.getOldValue());
            row.put("newValue", nullToDash(message));
            row.put("reason", nullToDash(reason));
        } else {
            row.put("info", message != null && !message.isBlank() ? message : buildChangeInfo(log, action, sbd));
            row.put("oldValue", null);
            row.put("newValue", nullToDash(message));
            row.put("reason", nullToDash(reason));
        }
        Timestamp createdAt = log.getCreatedAt();
        if (createdAt != null) {
            synchronized (AUDIT_TIME_FMT) {
                row.put("time", AUDIT_TIME_FMT.format(createdAt));
            }
            synchronized (AUDIT_DATE_FMT) {
                row.put("date", AUDIT_DATE_FMT.format(createdAt));
            }
        } else {
            row.put("time", "-");
            row.put("date", "-");
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

    private static AuditAction actionFromDb(String rawAction) {
        AuditAction action = AuditAction.fromValue(rawAction);
        return action != null ? action : AuditAction.UPDATE;
    }

    private static String entityLabelFromDb(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        AuditEntity entity = AuditEntity.fromValue(entityName);
        return entity != null ? entity.getValue() : entityName.trim();
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

    private String buildChangeInfo(Audit log, AuditAction action, String sbd) {
        String entity = entityLabelFromDb(log.getEntityName());
        String sbdSuffix = "-".equals(sbd) ? "" : " SBD " + sbd;
        return switch (action) {
            case CREATE ->
                "Thêm " + entity.toLowerCase() + sbdSuffix;
            case DELETE ->
                "Xóa " + entity.toLowerCase() + sbdSuffix;
            case EXPORT ->
                "Xuất " + entity.toLowerCase() + sbdSuffix;
            case IMPORT ->
                "Nhập " + entity.toLowerCase() + sbdSuffix;
            default ->
                "Cập nhật " + entity.toLowerCase() + sbdSuffix;
        };
    }

    private static String mapActionBadge(AuditAction action) {
        return switch (action) {
            case CREATE ->
                "audit-badge--insert";
            case DELETE ->
                "audit-badge--delete";
            case EXPORT ->
                "audit-badge--export";
            case IMPORT ->
                "audit-badge--import";
            default ->
                "audit-badge--update";
        };
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
