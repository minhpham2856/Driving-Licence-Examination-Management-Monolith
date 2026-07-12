package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;

public final class ReportExportLabels {

    private ReportExportLabels() {
    }

    // format section result
    public static String formatSectionResult(String passed) {
        if (passed == null || "none".equalsIgnoreCase(passed.trim())) {
            return "Chưa thi";
        }
        if ("passed".equalsIgnoreCase(passed)) {
            return "Đạt";
        }
        if ("failed".equalsIgnoreCase(passed)) {
            return "Trượt";
        }
        return passed;
    }

    public static String formatTheoryResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSkipsTheory()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getTheoryPassed());
    }

    public static String formatPracticalResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSkipsPractical()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getPracticalPassed());
    }

    public static String formatFinalResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSuspended()) {
            return "Đình chỉ";
        }
        if (reg.isAbsent()) {
            return "Vắng";
        }
        if (!reg.isExamFinished()) {
            return "Chưa xong";
        }
        return reg.isFinalPass() ? "Đạt" : "Trượt";
    // yes no
    }

    public static String yesNo(boolean value) {
    // safe file token
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
