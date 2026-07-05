package util;

import java.util.Locale;

public final class ExaminerExportFilenames {

    private ExaminerExportFilenames() {
    }

    public static String baseName(String documentType) {
        if (documentType == null || documentType.isBlank()) {
            return "tai-lieu";
        }
        return switch (documentType.trim().toLowerCase(Locale.ROOT)) {
            case "candidates" -> "danh-sach-thi-sinh";
            case "results" -> "ket-qua-thi";
            case "minutes" -> "bien-ban-thi";
            case "violations" -> "vi-pham";
            case "audit" -> "nhat-ky";
            case "bb1", "signature", "signature_form" -> "bb1-ly-thuyet";
            case "bb2", "layout", "score_sheet" -> "bb2-thuc-hanh-trong-hinh";
            case "bb3", "road" -> "bb3-thuc-hanh-tren-duong";
            default -> "tai-lieu";
        };
    }

    public static String withExtension(String documentType, String extension) {
        String ext = extension == null || extension.isBlank() ? "" : extension;
        if (!ext.isEmpty() && !ext.startsWith(".")) {
            ext = "." + ext;
        }
        return baseName(documentType) + ext;
    }

    public static String printCandidate(String documentType, int sbd) {
        return baseName(documentType) + "-sbd-" + sbd + ".docx";
    }
}
