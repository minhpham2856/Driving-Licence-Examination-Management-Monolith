package examstaff.util;

import examstaff.dto.user.AuditDTO;
import shared.enums.AuditEntity;
import java.util.Locale;

public final class AuditExportLabels {

    private AuditExportLabels() {
    }

    // apply display labels
    public static void applyDisplayLabels(AuditDTO log) {
        if (log == null) {
            return;
        }
        log.setEntityLabelVi(formatEntityLabel(log.getTableName()));
        log.setActionLabelVi(formatActionType(log));
        log.setDisplayDetails(formatOperationDetail(log));
    }

    // format action type
    public static String formatActionType(AuditDTO log) {
        if (log == null) {
            return "KhÃ¡c";
        }
        String details = log.getDetails();
        if (details != null) {
            String upper = details.toUpperCase(Locale.ROOT);
            if (upper.contains("RESET") || details.toLowerCase(Locale.ROOT).contains("xÃ³a há»“ sÆ¡ thá»§ tá»¥c")) {
                return "Äáº·t láº¡i thá»§ tá»¥c";
            }
            if (upper.contains("PHÃ‚N Bá»”") || upper.contains("ALLOCATE")) {
                return "PhÃ¢n bá»•";
            }
            if (upper.contains("THU PHÃ") || upper.contains("THANH TOÃN")) {
                return "Thu phÃ­";
            }
            if (upper.contains("IMPORT") || details.toLowerCase(Locale.ROOT).contains("nháº­p")) {
                return "Nháº­p dá»¯ liá»‡u";
            }
        }
        return formatActionCode(log.getAction());
        // format action code
    }

    public static String formatActionCode(String action) {
        if (action == null || action.isBlank()) {
            return "KhÃ¡c";
        }
        String upper = action.trim().toUpperCase(Locale.ROOT);
        if (upper.contains("WARNING")) {
            return "Cáº£nh bÃ¡o";
        }
        if (upper.contains("REMOVE") || upper.contains("DELETE")) {
            return "XÃ³a / Gá»¡";
        }
        return switch (upper) {
            case "INSERT" ->
                "ThÃªm má»›i";
            case "UPDATE" ->
                "Cáº­p nháº­t";
            case "DELETE" ->
                "XÃ³a";
            case "IMPORT" ->
                "Nháº­p dá»¯ liá»‡u";
            case "EXPORT" ->
                "Xuáº¥t dá»¯ liá»‡u";
            case "ASSIGN" ->
                "PhÃ¢n cÃ´ng / PhÃ¢n bá»•";
            default ->
                formatActionCodeFromPhrase(action.trim());
        };
        // format action code from phrase
    }

    private static String formatActionCodeFromPhrase(String action) {
        String upper = action.toUpperCase(Locale.ROOT);
        if (upper.contains("INSERT")) {
            return "ThÃªm má»›i";
        }
        if (upper.contains("UPDATE")) {
            return "Cáº­p nháº­t";
        }
        if (upper.contains("IMPORT")) {
            return "Nháº­p dá»¯ liá»‡u";
        }
        if (upper.contains("ASSIGN")) {
            return "PhÃ¢n cÃ´ng / PhÃ¢n bá»•";
        }
        if (upper.contains("EXPORT")) {
            return "Xuáº¥t dá»¯ liá»‡u";
        }
        // format entity label
        return action;
    }

    public static String formatEntityLabel(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "KhÃ¡c";
        }
        String trimmed = tableName.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT).replace(" ", "_");
        String mapped = switch (upper) {
            case "PAYMENT" ->
                "Thu phÃ­ thá»§ tá»¥c";
            case "EXAMREGISTRATION" ->
                "Há»“ sÆ¡ Ä‘Äƒng kÃ½ thi";
            case "PROFILE", "PERSON" ->
                "LÃ½ lá»‹ch thÃ­ sinh";
            case "EXAMSCORE" ->
                "Äiá»ƒm / Káº¿t quáº£ thi";
            case "SESSION" ->
                "Äiá»u hÃ nh ca thi";
            case "CANDIDATE", "EXAMENROLLMENT" ->
                "ThÃ­ sinh";
            case "EXAMINERSCHEDULE", "SESSION_EXAMINER" ->
                "PhÃ¢n cÃ´ng giÃ¡m thá»‹";
            case "SESSION_EXAMINERAREA" ->
                "PhÃ¢n cÃ´ng phÃ²ng giÃ¡m thá»‹";
            case "EXAMDEVICE" ->
                "Thiáº¿t bá»‹ thi";
            case "SCOREENTRYQUEUE" ->
                "HÃ ng Ä‘á»£i nháº­p Ä‘iá»ƒm";
            case "CANDIDATECALL" ->
                "Gá»i thÃ­ sinh";
            default ->
                null;
        };
        if (mapped != null) {
            return mapped;
        }
        String fromEnum = trimmed;
        if (fromEnum != null && !fromEnum.isBlank() && !fromEnum.equals(trimmed)) {
            return normalizeVietnameseEntityAlias(fromEnum);
        }
        // normalize vietnamese entity alias
        return normalizeVietnameseEntityAlias(trimmed);
    }

    private static String normalizeVietnameseEntityAlias(String label) {
        if (label == null || label.isBlank()) {
            return "KhÃ¡c";
        }
        return switch (label.trim()) {
            case "PhÃ¢n cÃ´ng sÃ¡t háº¡ch viÃªn" ->
                "PhÃ¢n cÃ´ng giÃ¡m thá»‹";
            case "PhÃ¢n cÃ´ng phÃ²ng sÃ¡t háº¡ch viÃªn" ->
                "PhÃ¢n cÃ´ng phÃ²ng giÃ¡m thá»‹";
            case "Thanh toÃ¡n" ->
                "Thu phÃ­ thá»§ tá»¥c";
            case "Äiá»ƒm thi", "Káº¿t quáº£ thi" ->
                "Äiá»ƒm / Káº¿t quáº£ thi";
            case "Ca thi" ->
                "Äiá»u hÃ nh ca thi";
            case "ThÃ­ sinh", "Há»“ sÆ¡ Ä‘Äƒng kÃ½ thi" ->
                label.trim();
            // format operation detail
            default ->
                label.trim();
        };
    }

    public static String formatOperationDetail(AuditDTO log) {
        if (log == null) {
            return "";
        }
        String details = log.getDetails();
        if (details != null && !details.isBlank()) {
            return normalizeOperationDetail(details.trim());
        }
        if (log.getReason() != null && !log.getReason().isBlank()) {
            return normalizeOperationDetail(log.getReason().trim());
        }
        if (log.getOldValue() != null && !log.getOldValue().isBlank()) {
            return normalizeOperationDetail(log.getOldValue().trim());
        }
        if (log.getNewValue() != null && !log.getNewValue().isBlank()) {
            return normalizeOperationDetail(log.getNewValue().trim());
        }
        return "";
    }

    private static String normalizeOperationDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return "";
        }
        String normalized = detail.replaceAll("\\s*SessionId=\\d+\\s*-\\s*", " ");
        normalized = normalized.replaceAll("\\s*SessionId=\\d+", "");
        normalized = normalized.replaceAll("(?i)\\buserId=(\\d+)", "mÃ£ ngÆ°á»i dÃ¹ng $1");
        normalized = normalized.replaceAll("(?i)\\bslot=([\\d:]+)", "phÃ¢n cÃ´ng $1");
        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }
}

