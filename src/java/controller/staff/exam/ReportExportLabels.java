package Controllers.Staff.ExamStaff;

import Models.ExamRegistration;

public final class ReportExportLabels {

    private ReportExportLabels() {
    }

    public static String formatSectionResult(String passed) {
        if (passed == null || "none".equalsIgnoreCase(passed.trim())) {
            return "Chưa thi";
        }
        if ("passed".equalsIgnoreCase(passed)) {
            return "Đạt";
        }
        if ("failed".equalsIgnoreCase(passed)) {
            return "Chưa đạt";
        }
        return passed;
    }

    public static String formatFinalResult(ExamRegistration reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isAbsent()) {
            return "Vắng/Đình chỉ";
        }
        if (!reg.isExamFinished()) {
            return "Chưa xong";
        }
        return reg.isFinalPass() ? "Đạt" : "Chưa đạt";
    }

    public static String yesNo(boolean value) {
        return value ? "Có" : "Chưa";
    }

    public static String safeFileToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return "ca_thi";
        }
        return raw.trim()
                .replaceAll("[^A-Za-z0-9\\-]", "_")
                .replaceAll("_+", "_");
    }

}
