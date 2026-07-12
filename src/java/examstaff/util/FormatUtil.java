package examstaff.util;

/** Format helpers used by examstaff registration/DAO (vendored subset). */
public final class FormatUtil {

    private FormatUtil() {
    }

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
