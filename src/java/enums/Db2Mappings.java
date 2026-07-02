package enums;

import model.user.Role;
import java.util.Map;

/**
 * Ánh xạ dữ liệu theo schema DLEM_DB_2.
 */
public final class Db2Mappings {

    private Db2Mappings() {
    }

    public static final Map<String, Integer> ROLE_NAME_TO_ID = Map.of(
            "Admin", 1,
            "Examiner", 2,
            "ManagingStaff", 3,
            "ExamStaff", 4,
            "Candidate", 5,
            "Registrant", 6
    );

    public static int roleIdFromName(String roleName) {
        if (roleName == null) {
            return 0;
        }
        return ROLE_NAME_TO_ID.getOrDefault(roleName, 0);
    }

    public static Role roleFromName(String roleName) {
        return new Role(roleIdFromName(roleName), roleName);
    }

    public static String sexFromGender(boolean gender) {
        return gender ? "Nữ" : "Nam";
    }

    public static boolean genderFromSex(String sex) {
        if (sex == null) {
            return false;
        }
        String s = sex.trim();
        return !(s.equalsIgnoreCase("Nam") || s.equalsIgnoreCase("Male") || s.equals("M"));
    }

    public static int parseCandidateNo(String candidateNumber) {
        if (candidateNumber == null || !candidateNumber.contains("-")) {
            return 0;
        }
        try {
            return Integer.parseInt(candidateNumber.substring(candidateNumber.indexOf('-') + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Tiền tố CandidateNumber tạm khi thí sinh đăng ký online, chưa được cán bộ import SBD. */
    public static final String PENDING_SBD_PREFIX = "PENDING-SBD-";

    public static String buildCandidateNumber(String licenseCode, int candidateNo) {
        String lc = licenseCode != null ? licenseCode : "XX";
        return lc + "-" + String.format("%04d", candidateNo);
    }

    public static String buildPendingCandidateNumber(int profileId, int sessionId) {
        return PENDING_SBD_PREFIX + profileId + "-" + sessionId;
    }

    public static boolean isPendingCandidateNumber(String candidateNumber) {
        return candidateNumber != null && candidateNumber.startsWith(PENDING_SBD_PREFIX);
    }

    public static boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null) {
            return false;
        }
        return switch (registrationStatus) {
            case "CheckedIn", "Present", "Completed" -> true;
            default -> false;
        };
    }
}
