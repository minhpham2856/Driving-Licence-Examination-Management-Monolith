package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

/** Nhãn tiếng Việt cho xuất báo cáo kết quả thi. */
public final class ReportExportLabels {

    private ReportExportLabels() {
    }

    /**
     * Đổi cờ đạt/trượt phần thi thành nhãn hiển thị.
     *
     * @param passed {@code passed}/{@code failed}/{@code none}/khác
     * @return nhãn tiếng Việt hoặc chuỗi gốc
     */
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

    /**
     * Kết quả lý thuyết (kể cả bảo lưu).
     *
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
    public static String formatTheoryResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSkipsTheory()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getTheoryPassed());
    }

    /**
     * Kết quả thực hành (kể cả bảo lưu).
     *
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
    public static String formatPracticalResult(ExamRegistrationDTO reg) {
        if (reg == null) {
            return "";
        }
        if (reg.isSkipsPractical()) {
            return "Bảo lưu";
        }
        return formatSectionResult(reg.getPracticalPassed());
    }

    /**
     * Kết quả tổng (đình chỉ / vắng / chưa xong / đạt / trượt).
     *
     * @param reg hồ sơ đăng ký
     * @return nhãn hoặc chuỗi rỗng nếu {@code reg == null}
     */
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
    }

    /**
     * Boolean → Có / Chưa.
     *
     * @param value giá trị
     * @return {@code "Có"} hoặc {@code "Chưa"}
     */
    public static String yesNo(boolean value) {
        return value ? "Có" : "Chưa";
    }

    /**
     * Chuẩn hóa token an toàn cho tên file (ký tự lạ → {@code _}).
     *
     * @param raw chuỗi gốc (blank → {@code "ca_thi"})
     * @return token file
     */
    public static String safeFileToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return "ca_thi";
        }
        return raw.trim()
                .replaceAll("[^A-Za-z0-9\\-]", "_")
                .replaceAll("_+", "_");
    }

}
