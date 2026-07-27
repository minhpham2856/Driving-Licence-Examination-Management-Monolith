package examiner.service.impl;

import examiner.dao.AuditDAO;
import examiner.dao.ProfileDAO;
import examiner.dao.UserDAO;
import examiner.dao.impl.AuditDAOImpl;
import examiner.dao.impl.ProfileDAOImpl;
import examiner.dao.impl.UserDAOImpl;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.model.Audit;
import shared.model.Profile;
import shared.model.Role;
import shared.model.User;
import shared.enums.RoleType;
import examiner.service.AuditService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import examiner.service.RoleService;
import java.util.stream.Collectors;

// Implements examiner audit business logic.
public class AuditServiceImpl implements AuditService {

    private final AuditDAO DAO = new AuditDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private static final Pattern SBD_PATTERN = Pattern.compile("SBD\\s+([A-Za-z0-9]+-\\d+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final SimpleDateFormat AUDIT_TIME_FMT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    // Writes a simple audit log entry without record id or reason.
    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message) {
        logAction(userId, action, entity, message, 0);
    }

    // Writes an audit log entry tied to a specific record id.
    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message, int recordId) {
        logAction(userId, action, entity, message, recordId, null);
    }

    // Writes an audit log entry with record id and optional reason text.
    @Override
    public void logAction(Integer userId, AuditAction action, AuditEntity entity, String message,
            int recordId, String reason) {
        insertLog(userId, action, entity, message, recordId, reason);
    }

    // Private helper: insert log.
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
            DAO.add(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Converts one audit row into one or more JSP-friendly view row maps.
    @Override
    public List<Map<String, Object>> toViewRows(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        return List.of(toViewRow(log, changerName, sbdByRecordId));
    }

    // Converts one audit row into a single JSP-friendly view row map.
    @Override
    public Map<String, Object> toViewRow(Audit log, String changerName, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        AuditAction action = actionFromDb(log.getAction());
        String sbd = extractSbdForDisplay(log, sbdByRecordId);
        String message = firstNonBlank(log.getNewValue(), log.getDetails());
        String reason = normalizeReason(log);
        row.put("username", nullToDash(changerName));
        row.put("actionLabel", action.getValue());
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", entityLabelFromDb(log.getEntityName()));
        row.put("sbd", sbd);
        row.put("newValueClass", action == AuditAction.DELETE ? "audit-td-old" : "audit-td-new");
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

    // Extracts candidate number (SBD) text from audit fields for display.
    @Override
    public String extractSbdForDisplay(Audit log, Map<Integer, String> sbdByRecordId) {
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

    // Maps raw DB action string to AuditAction enum with UPDATE fallback.
    private static AuditAction actionFromDb(String rawAction) {
        AuditAction action = AuditAction.fromValue(rawAction);
        return action != null ? action : AuditAction.UPDATE;
    }

    // Maps raw entity name from DB to AuditEntity label when known.
    private static String entityLabelFromDb(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        AuditEntity entity = AuditEntity.fromValue(entityName);
        return entity != null ? entity.getValue() : entityName.trim();
    }

    // Private helper: parse entity id.
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

    // Private helper: extract sbd from text.
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

    // Private helper: normalize reason.
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

    // Private helper: build change info.
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

    // Private helper: map action badge.
    private static String mapActionBadge(AuditAction action) {
        return switch (action) {
            case CREATE ->
                "audit-badge-insert";
            case DELETE ->
                "audit-badge-delete";
            case EXPORT ->
                "audit-badge-export";
            case IMPORT ->
                "audit-badge-import";
            default ->
                "audit-badge-update";
        };
    }

    // Returns dash placeholder for blank display strings.
    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    // Private helper: first non blank.
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

    // Loads paginated audit logs for an exam with optional search filter.
    @Override
    public List<Audit> getAllByExam(int examId, int page, int pageSize, String searchQuery) {
        return DAO.getAllByExam(examId, page, pageSize, searchQuery);
    }

    // Returns total audit log count for an exam matching the search filter.
    @Override
    public int countAllByExam(int examId, String searchQuery) {
        return DAO.countAllByExam(examId, searchQuery);
    }

    // Loads recent violation-related audit logs for an exam up to a limit.
    @Override
    public List<Audit> getAllViolationsByExam(int examId, int limit) {
        return DAO.getAllViolationsByExam(examId, limit);
    }

    // Resolves display names for users who performed audited actions.
    @Override
    public Map<Long, String> getAllChangerNamesByAudit(List<Audit> audits) {
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

    private final RoleService roleService = new RoleServiceImpl();
    private static final SimpleDateFormat ADMIN_TS_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Personal audit logs for one user, optionally restricted to a single day.
    @Override
    public List<Audit> getAllByUser(int userId, String dateFilter) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return DAO.getAllByUser(userId, dateFilter);
    }

    // Searches audit logs by keyword and maps them to admin-style view rows.
    @Override
    public List<Map<String, Object>> getFiltered(String keyword, int limit) {
        List<Audit> audits = DAO.getFiltered(keyword, limit);
        List<Integer> userIds = new ArrayList<>();
        for (Audit audit : audits) {
            if (audit.getUserId() != null && !userIds.contains(audit.getUserId())) {
                userIds.add(audit.getUserId());
            }
        }
        Map<Integer, User> users = new HashMap<>();
        for (User user : userDAO.getAllByIds(userIds)) {
            users.put(user.getUserId(), user);
        }
        Map<Integer, Profile> profiles = new HashMap<>();
        for (Profile profile : profileDAO.getAllByUserIds(userIds)) {
            profiles.put(profile.getUserId(), profile);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Audit audit : audits) {
            rows.add(toAdminViewRow(audit, users, profiles));
        }
        return rows;
    }

    // Private helper: to admin view row.
    private Map<String, Object> toAdminViewRow(Audit audit, Map<Integer, User> users, Map<Integer, Profile> profiles) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(audit.getAuditId()));
        if (audit.getCreatedAt() != null) {
            synchronized (ADMIN_TS_FMT) {
                row.put("timestamp", ADMIN_TS_FMT.format(audit.getCreatedAt()));
            }
        } else {
            row.put("timestamp", "-");
        }
        User user = audit.getUserId() != null ? users.get(audit.getUserId()) : null;
        Profile profile = audit.getUserId() != null ? profiles.get(audit.getUserId()) : null;
        String username = user != null ? user.getUsername() : "he_thong";
        String fullName = profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()
                ? profile.getFullName()
: (user != null ? user.getUsername() : "Hệ thống");
        row.put("username", username);
        row.put("fullName", fullName);
        String roleKey = user != null ? mapRoleKey(user.getRoleId()) : "admin";
        row.put("roleKey", roleKey);
        row.put("role", mapRoleLabel(roleKey));
        row.put("avatarClass", mapAvatarClass(roleKey));
        String action = audit.getAction() != null ? audit.getAction() : "-";
        row.put("action", action);
        row.put("actionKey", mapActionKey(action));
        row.put("module", audit.getEntityName() != null ? audit.getEntityName() : "-");
        String details = firstNonBlank(audit.getNewValue(), audit.getDetails(), audit.getReason());
        row.put("details", details != null ? details : "-");
        row.put("ip", "-");
        row.put("ipAddress", "-");
        row.put("device", "-");
        row.put("status", "Thành công");
        row.put("statusKey", "success");
        return row;
    }

    // Private helper: map role key.
    private String mapRoleKey(int roleId) {
        Role roleEntity = roleService.get(roleId);
        String roleName = roleEntity != null ? roleEntity.getRoleName() : null;
        RoleType role = RoleType.fromValue(roleName);
        if (role == RoleType.ADMIN) {
            return "admin";
        }
        if (role == RoleType.EXAM_STAFF) {
            return "coi";
        }
        if (role == RoleType.EXAMINER) {
            return "cham";
        }
        return "coi";
    }

    // Private helper: map role label.
    private static String mapRoleLabel(String roleKey) {
        if ("admin".equals(roleKey)) {
            return "Quản trị viên";
        }
        if ("coi".equals(roleKey)) {
            return "Cán bộ coi thi";
        }
        if ("cham".equals(roleKey)) {
            return "sát hạch viên";
        }
        return roleKey;
    }

    // Private helper: map avatar class.
    private static String mapAvatarClass(String roleKey) {
        if ("coi".equals(roleKey)) {
            return "user-avatar--teal";
        }
        if ("cham".equals(roleKey)) {
            return "user-avatar--purple";
        }
        return "";
    }

    // Private helper: map action key.
    private static String mapActionKey(String action) {
        if (action == null) {
            return "info";
        }
        String lower = action.toLowerCase();
        if (lower.contains("xóa") || lower.contains("delete") || lower.contains("đình chỉ")) {
            return "danger";
        }
        if (lower.contains("cảnh báo") || lower.contains("khóa")) {
            return "warning";
        }
        if (lower.contains("tạo") || lower.contains("create") || lower.contains("import")) {
            return "success";
        }
        return "info";
    }
}

