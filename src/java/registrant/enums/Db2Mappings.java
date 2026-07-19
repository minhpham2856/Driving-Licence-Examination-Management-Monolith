package registrant.enums;

/**
 * Ánh xạ dữ liệu theo schema DLEM_DB_2 dùng bởi cổng Registrant.
 */
public final class Db2Mappings {

    private Db2Mappings() {
    }

    /** Tiền tố CandidateNumber tạm khi thí sinh đăng ký online, chưa được cán bộ import SBD. */
    public static final String PENDING_SBD_PREFIX = "PENDING-SBD-";

    /** True nếu SBD còn dạng tạm PENDING-SBD-*. */
    public static boolean isPendingCandidateNumber(String candidateNumber) {
        return candidateNumber != null && candidateNumber.startsWith(PENDING_SBD_PREFIX);
    }
}
