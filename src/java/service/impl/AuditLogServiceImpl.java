package service.impl;

import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import model.user.AuditRecordModel;
import model.user.Audit;
import model.user.User;
import model.user.Profile;
import dao.UserDAO;
import dao.impl.UserDAOImpl;
import dao.ProfileDAO;
import dao.impl.ProfileDAOImpl;
import java.util.stream.Collectors;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import service.AuditLogService;
import service.EnumMappingService;
import util.AuditChangeDetails;

public class AuditLogServiceImpl implements AuditLogService {

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();
    private final AuditDAO DAO = new AuditDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();

    private static final Pattern SBD_PATTERN = Pattern.compile("SBD\\s+([A-Za-z0-9]+-\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Override
    public void persist(Integer actionUserId, String action, String details) {
        persist(actionUserId, action, details, 0);
    }

    @Override
    public boolean insertAudit(model.user.Audit audit) { return DAO.insert(audit) > 0; }

    @Override
    public void persist(Integer actionUserId, String action, String details, int recordId) {
        insertLog(actionUserId, action, details, null, details, null, null, recordId);
    }

    @Override
    public void persistChange(Integer actionUserId, String action, String details,
            String oldValue, String newValue, String reason, int recordId) {
        insertLog(actionUserId, action, details, oldValue, newValue, reason, null, recordId);
    }

    @Override
    public void persistFieldChanges(Integer actionUserId, String action, String contextDetails,
            List<AuditChangeDetails.FieldChange> changes, String reason, int recordId) {
        if (changes == null || changes.isEmpty()) {
            return;
        }
        for (AuditChangeDetails.FieldChange change : changes) {
            String detailsJson = AuditChangeDetails.toJson(List.of(change));
            insertLog(actionUserId, action, contextDetails, null, null, reason, detailsJson, recordId);
        }
    }

    private void insertLog(Integer actionUserId, String action, String contextDetails,
            String oldValue, String newValue, String reason, String detailsJson, int recordId) {
        try {
            int userId = (actionUserId != null && actionUserId > 0) ? actionUserId : 3;

            Audit log = new Audit();
            log.setEntityName(enumMappingService.auditLabel(resolveEntityName(action, contextDetails)));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction(normalizeAction(action));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setReason(reason);
            log.setDetails(detailsJson);
            log.setUserId(userId);
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            DAO.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void persistWarning(Integer actionUserId, String details, String reason, int recordId) {
        try {
            
            int userId = (actionUserId != null && actionUserId > 0) ? actionUserId : 3;

            Audit log = new Audit();
            log.setEntityName(enumMappingService.auditLabel("Candidate"));
            log.setEntityId(String.valueOf(recordId > 0 ? recordId : 0));
            log.setAction("WARNING");
            log.setNewValue(details);
            log.setReason(reason);
            log.setDetails(AuditChangeDetails.toJson(List.of(
                    new AuditChangeDetails.FieldChange("TrÃƒÂ¡Ã‚ÂºÃ‚Â¡ng thÃƒÆ’Ã‚Â¡i", "HoÃƒÂ¡Ã‚ÂºÃ‚Â¡t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng bÃƒÆ’Ã‚Â¬nh thÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âng", "Ãƒâ€žÃ‚ÂÃƒÆ’Ã‚Â¬nh chÃƒÂ¡Ã‚Â»Ã¢â‚¬Â°"))));
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

        if (upper.contains("SCOREENTRY") || detailUpper.contains("HÃƒÆ’Ã¢â€šÂ¬NG Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã‚Â¢I")) {
            return "ScoreEntryQueue";
        }
        if (upper.contains("EXAMDEVICE") || detailUpper.contains("THIÃƒÂ¡Ã‚ÂºÃ‚Â¾T BÃƒÂ¡Ã‚Â»Ã…Â ")) {
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
            return "Session_Examiner";
        }
        if (detailUpper.contains("Ãƒâ€žÃ‚ÂIÃƒÂ¡Ã‚Â»Ã¢â‚¬Å¡M") || detailUpper.contains("DIEM")
                || upper.contains("EXAMSCORE") || detailUpper.contains("LÃƒÆ’Ã‚Â THUYÃƒÂ¡Ã‚ÂºÃ‚Â¾T")
                || detailUpper.contains("THÃƒÂ¡Ã‚Â»Ã‚Â°C HÃƒÆ’Ã¢â€šÂ¬NH") || detailUpper.contains("Ãƒâ€žÃ‚ÂÃƒâ€ Ã‚Â¯ÃƒÂ¡Ã‚Â»Ã…â€œNG TRÃƒâ€ Ã‚Â¯ÃƒÂ¡Ã‚Â»Ã…â€œNG")) {
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

    @Override
    public List<Map<String, Object>> toViewRows(AuditRecordModel log, Map<Integer, String> sbdByRecordId) {
        List<AuditChangeDetails.FieldChange> changes = AuditChangeDetails.parseChanges(log.getDetails());
        if (changes.size() <= 1) {
            return List.of(toViewRow(log, sbdByRecordId));
        }
        List<Map<String, Object>> rows = new ArrayList<>(changes.size());
        for (AuditChangeDetails.FieldChange change : changes) {
            rows.add(toViewRowForFieldChange(log, sbdByRecordId, change));
        }
        return rows;
    }

    @Override
    public Map<String, Object> toViewRow(AuditRecordModel log, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String reason = normalizeReason(log);

        AuditChangeDetails.DisplayColumns columns = AuditChangeDetails.toDisplayColumns(
                log.getDetails(), log.getOldValue(), log.getNewValue());
        boolean hasFieldChanges = columns.info() != null;

        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", enumMappingService.auditLabel(log.getTableName()));
        row.put("sbd", sbd);
        row.put("newValueClass", mapNewValueClass(action));
        row.put("multiline", columns.multiline());

        if (hasFieldChanges) {
            row.put("info", columns.info());
            row.put("oldValue", columns.oldValue());
            row.put("newValue", columns.newValue());
            row.put("reason", nullToDash(reason));
        } else if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            row.put("info", buildChangeInfo(log, sbd));
            row.put("oldValue", log.getOldValue());
            row.put("newValue", nullToDash(log.getNewValue()));
            row.put("reason", nullToDash(reason));
            row.put("multiline", log.getOldValue().contains(";"));
        } else {
            row.put("info", "-");
            row.put("oldValue", null);
            row.put("newValue", nullToDash(log.getNewValue()));
            row.put("reason", nullToDash(reason));
        }
        return row;
    }

    private Map<String, Object> toViewRowForFieldChange(AuditRecordModel log, Map<Integer, String> sbdByRecordId,
            AuditChangeDetails.FieldChange change) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String reason = normalizeReason(log);

        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", enumMappingService.auditLabel(log.getTableName()));
        row.put("sbd", sbd);
        row.put("newValueClass", mapNewValueClass(action));
        row.put("multiline", false);
        row.put("info", "Thay Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢i " + change.field().toLowerCase());
        row.put("oldValue", nullToDash(change.oldValue()));
        row.put("newValue", nullToDash(change.newValue()));
        row.put("reason", nullToDash(reason));
        return row;
    }

    @Override
    public String resolveSbd(AuditRecordModel log, Map<Integer, String> sbdByRecordId) {
        for (String text : new String[]{log.getNewValue(), log.getOldValue(), log.getReason(), log.getDetails()}) {
            String extracted = extractSbdFromText(text);
            if (extracted != null) {
                return extracted;
            }
        }
        if (log.getRecordId() != null && log.getRecordId() > 0 && sbdByRecordId != null) {
            String mapped = sbdByRecordId.get(log.getRecordId());
            if (mapped != null && !mapped.isBlank()) {
                return mapped;
            }
        }
        return "-";
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

    private String normalizeReason(AuditRecordModel log) {
        String reason = log.getReason();
        if (reason == null || reason.isBlank()) {
            return null;
        }
        if (log.getDetails() == null || log.getDetails().isBlank()) {
            if (log.getOldValue() == null || log.getOldValue().isBlank()) {
                if (log.getNewValue() != null && reason.equals(log.getNewValue())) {
                    return null;
                }
            }
        }
        return reason;
    }

    private String buildChangeInfo(AuditRecordModel log, String sbd) {
        String entity = enumMappingService.auditLabel(log.getTableName());
        String action = log.getAction() != null ? log.getAction().toUpperCase() : "UPDATE";
        String sbdSuffix = "-".equals(sbd) ? "" : " SBD " + sbd;
        return switch (action) {
            case "WARNING" ->
                "CÃƒÂ¡Ã‚ÂºÃ‚Â£nh bÃƒÆ’Ã‚Â¡o" + sbdSuffix;
            case "INSERT" ->
                "ThÃƒÆ’Ã‚Âªm " + entity.toLowerCase() + sbdSuffix;
            case "DELETE" ->
                "XÃƒÆ’Ã‚Â³a " + entity.toLowerCase() + sbdSuffix;
            default ->
                "CÃƒÂ¡Ã‚ÂºÃ‚Â­p nhÃƒÂ¡Ã‚ÂºÃ‚Â­t " + entity.toLowerCase() + sbdSuffix;
        };
    }

    private String mapActionLabel(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" ->
                "ThÃƒÆ’Ã‚Âªm";
            case "DELETE" ->
                "XÃƒÆ’Ã‚Â³a";
            case "EXPORT" ->
                "XuÃƒÂ¡Ã‚ÂºÃ‚Â¥t";
            case "ASSIGN" ->
                "PhÃƒÆ’Ã‚Â¢n cÃƒÆ’Ã‚Â´ng";
            case "IMPORT" ->
                "NhÃƒÂ¡Ã‚ÂºÃ‚Â­p";
            case "WARNING" ->
                "CÃƒÂ¡Ã‚ÂºÃ‚Â£nh bÃƒÆ’Ã‚Â¡o";
            case "SYSTEM" ->
                "HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng";
            case "APPROVE" ->
                "DuyÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t";
            default ->
                "CÃƒÂ¡Ã‚ÂºÃ‚Â­p nhÃƒÂ¡Ã‚ÂºÃ‚Â­t";
        };
    }

    private String mapActionBadge(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" ->
                "audit-badge--insert";
            case "DELETE" ->
                "audit-badge--delete";
            case "EXPORT" ->
                "audit-badge--export";
            case "ASSIGN" ->
                "audit-badge--assign";
            case "IMPORT" ->
                "audit-badge--import";
            case "WARNING" ->
                "audit-badge--warning";
            case "SYSTEM" ->
                "audit-badge--system";
            case "APPROVE" ->
                "audit-badge--approve";
            default ->
                "audit-badge--update";
        };
    }

    private String mapNewValueClass(String action) {
        if ("DELETE".equalsIgnoreCase(action)) {
            return "audit-td--old";
        }
        return "audit-td--new";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @Override
    public List<AuditRecordModel> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        List<Audit> audits = DAO.getLogsForSessionPaginated(sessionId, page, pageSize, searchQuery);
        if (audits == null || audits.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> userIds = audits.stream()
                .map(Audit::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = userDAO.findByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Integer, Profile> profileMap = profileDAO.findByUserIds(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        List<AuditRecordModel> result = new ArrayList<>();
        for (Audit a : audits) {
            AuditRecordModel rm = new AuditRecordModel();
            rm.setId(a.getAuditId());
            rm.setTableName(a.getEntityName());
            try {
                rm.setRecordId(Integer.parseInt(a.getEntityId()));
            } catch (NumberFormatException e) {
                rm.setRecordId(null);
            }
            rm.setAction(a.getAction());
            rm.setOldValue(a.getOldValue());
            rm.setNewValue(a.getNewValue());
            rm.setDetails(a.getDetails());
            rm.setReason(a.getReason());
            rm.setChangedBy(a.getUserId());
            rm.setChangedAt(a.getCreatedAt());

            User u = userMap.get(a.getUserId());
            Profile p = profileMap.get(a.getUserId());

            if (u != null && u.getUsername() != null) {
                rm.setChangerName(u.getUsername());
            } else if (p != null && p.getFullName() != null) {
                rm.setChangerName(p.getFullName());
            } else {
                rm.setChangerName("Unknown");
            }

            result.add(rm);
        }
        return result;
    }

    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        return DAO.getLogsCountForSession(sessionId, searchQuery);
    }

    @Override
    public List<AuditRecordModel> getViolationLogsForSession(int sessionId, int limit) {
        List<Audit> audits = DAO.getViolationLogsForSession(sessionId, limit);
        if (audits == null || audits.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> userIds = audits.stream()
                .map(Audit::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = userDAO.findByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Integer, Profile> profileMap = profileDAO.findByUserIds(userIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, p -> p));

        List<AuditRecordModel> result = new ArrayList<>();
        for (Audit a : audits) {
            AuditRecordModel rm = new AuditRecordModel();
            rm.setId(a.getAuditId());
            rm.setTableName(a.getEntityName());
            try {
                rm.setRecordId(Integer.parseInt(a.getEntityId()));
            } catch (NumberFormatException e) {
                rm.setRecordId(null);
            }
            rm.setAction(a.getAction());
            rm.setOldValue(a.getOldValue());
            rm.setNewValue(a.getNewValue());
            rm.setDetails(a.getDetails());
            rm.setReason(a.getReason());
            rm.setChangedBy(a.getUserId());
            rm.setChangedAt(a.getCreatedAt());

            User u = userMap.get(a.getUserId());
            Profile p = profileMap.get(a.getUserId());

            if (u != null && u.getUsername() != null) {
                rm.setChangerName(u.getUsername());
            } else if (p != null && p.getFullName() != null) {
                rm.setChangerName(p.getFullName());
            } else {
                rm.setChangerName("Unknown");
            }

            result.add(rm);
        }
        return result;
    }
}


