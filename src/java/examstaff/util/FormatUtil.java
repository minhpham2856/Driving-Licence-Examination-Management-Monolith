package examstaff.util;

/** Parse số báo danh / số thí sinh từ chuỗi (có hoặc không tiền tố). */
public final class FormatUtil {

    private FormatUtil() {
    }

    /**
     * Lấy phần số từ candidate number (sau dấu {@code -} nếu có).
     *
     * @param candidateNumber chuỗi SBD/số thí sinh
     * @return số nguyên hoặc {@code 0} nếu không parse được
     */
    public static int parseCandidateNo(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return 0;
        }
        String trimmed = candidateNumber.trim();
        if (trimmed.contains("-")) {
            try {
                return Integer.parseInt(trimmed.substring(trimmed.indexOf('-') + 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
