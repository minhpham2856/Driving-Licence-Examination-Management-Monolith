package examiner.util;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public final class FormatUtil {

    private FormatUtil() {
    }

    // Normalize document type aliases (e.g. minutes -> result) to canonical lowercase names.
    public static String formatDocumentType(String type) {
        if (type == null) {
            return "";
        }
        String normalized = type.trim().toLowerCase();
        return switch (normalized) {
            case "minutes" ->
                "result";
            case "paper", "exam-paper", "de-thi", "theory", "bb1", "bb1-ly-thuyet" ->
                "theory";
            case "layout", "bb2", "bb2-thuc-hanh-trong-hinh", "score-sheet" ->
                "layout";
            case "violation", "violation-minutes", "bien-ban-vi-pham" ->
                "violation";
            default ->
                normalized;
        };
    }

    // Return true when the document type is a session-wide table export or print.
    public static boolean isSessionDocumentType(String type) {
        String normalized = formatDocumentType(type);
        return switch (normalized) {
            case "candidates", "result", "violations", "audit" ->
                true;
            default ->
                false;
        };
    }

    // Return true when the document type is a per-candidate result form requiring a valid SBD.
    public static boolean isCandidateResultDocument(String type, int sbd) {
        if (sbd <= 0) {
            return false;
        }
        String normalized = formatDocumentType(type);
        return "result".equals(normalized)
                || "theory".equals(normalized)
                || "layout".equals(normalized)
                || "violation".equals(normalized);
    }

    // Parse optional positive SBD filter from a raw query parameter.
    public static Integer formatSbdFilter(String raw) {
        return shared.util.FormatUtil.formatPositiveInteger(raw);
    }

    // Walk the exception cause chain and return the first non-blank IOException message.
    public static String resolveDocumentErrorMessage(Throwable ex, String fallbackMessage) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof IOException) {
                String message = cur.getMessage();
                if (message != null && !message.isBlank()) {
                    return message;
                }
            }
            cur = cur.getCause();
        }
        return fallbackMessage;
    }

    // Build audit message describing what was printed.
    public static String formatPrintAuditMessage(String type, int sbd) {
        String normalized = formatDocumentType(type);
        String label = switch (normalized) {
            case "candidates" ->
                "danh sách thí sinh";
            case "result", "results" ->
                sbd > 0 ? "biên bản kết quả thi" : "tổng hợp kết quả thi";
            case "violations" ->
                "danh sách thí sinh vi phạm";
            case "violation" ->
                sbd > 0 ? "biên bản vi phạm" : "danh sách thí sinh vi phạm";
            case "audit" ->
                "nhật ký";
            case "theory" ->
                "đề thi";
            case "layout" ->
                "biên bản thực hành";
            default ->
                type != null ? type : "tài liệu";
        };
        if (sbd > 0) {
            return "In " + label + " SBD " + sbd;
        }
        return "In " + label;
    }

    // Build the page title for print preview header (session table exports).
    public static String formatPrintTitle(String type, int sbd) {
        String normalized = formatDocumentType(type);
        String label = switch (normalized) {
            case "candidates" ->
                "Danh sách thí sinh";
            case "result", "results" ->
                sbd > 0 ? "Biên bản kết quả thi" : "Tổng hợp kết quả thi";
            case "violations" ->
                sbd > 0 ? "Biên bản vi phạm" : "Danh sách thí sinh vi phạm";
            case "audit" ->
                "Nhật ký";
            default ->
                "Văn bản in";
        };
        if (sbd > 0) {
            return label + " - SBD " + sbd;
        }
        return label;
    }

    // Build the page title for per-candidate print preview.
    public static String formatBbPrintTitle(String type, int sbd) {
        String normalized = formatDocumentType(type);
        String label = switch (normalized) {
            case "result", "results" ->
                "Biên bản kết quả thi";
            case "theory" ->
                "Biên bản sát hạch lý thuyết";
            case "layout" ->
                "Biên bản sát hạch thực hành";
            case "violation" ->
                "Biên bản vi phạm";
            default ->
                "Văn bản in";
        };
        if (sbd > 0) {
            return label + " - SBD " + sbd;
        }
        return label;
    }

    // Parse candidate number from sbd or returnSbd request parameter.
    public static Integer formatSbdFromRequest(HttpServletRequest request) {
        Integer sbd = shared.util.FormatUtil.formatPositiveInteger(request.getParameter("sbd"));
        if (sbd != null) {
            return sbd;
        }
        return shared.util.FormatUtil.formatPositiveInteger(request.getParameter("returnSbd"));
    }
}
