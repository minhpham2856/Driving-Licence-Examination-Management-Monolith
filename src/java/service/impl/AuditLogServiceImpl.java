package service.impl;

import dao.AuditDAO;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import model.Audit;
import model.AuditRecordModel;
import model.Profile;
import model.User;
import service.AuditLogService;
import service.EnumMappingService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuditLogServiceImpl implements AuditLogService {

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();
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
        insertLog(userId, "WARNING", message, null, message, reason, recordId);
    }

    private void insertLog(Integer actionUserId, String action, String contextDetails,
            String oldValue, String newValue, String reason, int recordId) {
        try {
            int userId = (actionUserId != null && actionUserId > 0) ? actionUserId : 3;

            Audit log = new Audit();
            log.setEntityName(enumMappingService.auditLabel(resolveEntityName(action, contextDetails)));
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
        return List.of(toViewRow(log, sbdByRecordId));
    }

    @Override
    public Map<String, Object> toViewRow(AuditRecordModel log, Map<Integer, String> sbdByRecordId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        String sbd = resolveSbd(log, sbdByRecordId);
        String message = firstNonBlank(log.getNewValue(), log.getDetails());
        String reason = normalizeReason(log);

        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", enumMappingService.auditLabel(log.getTableName()));
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
        if (log.getOldValue() == null || log.getOldValue().isBlank()) {
            if (log.getNewValue() != null && reason.equals(log.getNewValue())) {
                return null;
            }
        }
        return reason;
    }

    private String buildChangeInfo(AuditRecordModel log, String sbd) {
        String entity = enumMappingService.auditLabel(log.getTableName());
        String action = log.getAction() != null ? log.getAction().toUpperCase() : "UPDATE";
        String sbdSuffix = "-".equals(sbd) ? "" : " SBD " + sbd;
        return switch (action) {
            case "WARNING" -> "Cảnh báo" + sbdSuffix;
            case "INSERT" -> "Thêm " + entity.toLowerCase() + sbdSuffix;
            case "DELETE" -> "Xóa " + entity.toLowerCase() + sbdSuffix;
            default -> "Cập nhật " + entity.toLowerCase() + sbdSuffix;
        };
    }

    private String mapActionLabel(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "Thêm";
            case "DELETE" -> "Xóa";
            case "EXPORT" -> "Xuất";
            case "ASSIGN" -> "Phân công";
            case "IMPORT" -> "Nhập";
            case "WARNING" -> "Cảnh báo";
            case "SYSTEM" -> "Hệ thống";
            case "APPROVE" -> "Duyệt";
            default -> "Cập nhật";
        };
    }

    private String mapActionBadge(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "audit-badge--insert";
            case "DELETE" -> "audit-badge--delete";
            case "EXPORT" -> "audit-badge--export";
            case "ASSIGN" -> "audit-badge--assign";
            case "IMPORT" -> "audit-badge--import";
            case "WARNING" -> "audit-badge--warning";
            case "SYSTEM" -> "audit-badge--system";
            case "APPROVE" -> "audit-badge--approve";
            default -> "audit-badge--update";
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
    public List<AuditRecordModel> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        List<Audit> audits = DAO.getLogsForSessionPaginated(sessionId, page, pageSize, searchQuery);
        return mapAuditRecords(audits);
    }

    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        return DAO.getLogsCountForSession(sessionId, searchQuery);
    }

    @Override
    public List<AuditRecordModel> getViolationLogsForSession(int sessionId, int limit) {
        List<Audit> audits = DAO.getViolationLogsForSession(sessionId, limit);
        return mapAuditRecords(audits);
    }

    private List<AuditRecordModel> mapAuditRecords(List<Audit> audits) {
        if (audits == null || audits.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> userIds = audits.stream()
                .map(Audit::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = userDAO.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Integer, Profile> profileMap = profileDAO.getAllByUserIds(userIds).stream()
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
