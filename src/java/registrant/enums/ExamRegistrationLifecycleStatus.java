package registrant.enums;

/**
 * Hằng số trạng thái vòng đời đăng ký ca thi trên ExamRegistration — tách khỏi workflow tài liệu Draft/Pending/Approved/Rejected.
 * Cung cấp mệnh đề SQL (SQL_LIFECYCLE_ONLY, SQL_EXCLUDE_PROFILE_DOC) lọc ER thi thật khỏi bản ghi hồ sơ tài liệu (#PROFILE_DOC#, #LICENCE_DOC#) trong dashboard/my-exams.
 */
public final class ExamRegistrationLifecycleStatus {

    public static final String PRE_REGISTERED = "PreRegistered";
    public static final String CANCEL_REQUESTED = "CancelRequested";
    public static final String REGISTRATION_REJECTED = "RegistrationRejected";
    public static final String CANCELLED = "Cancelled";
    public static final String CHECKED_IN = "CheckedIn";
    public static final String PRESENT = "Present";
    public static final String COMPLETED = "Completed";
    public static final String WALK_IN = "WalkIn";

    /** SQL: chỉ lấy ER lifecycle (không lấy hồ sơ duyệt tài liệu). */
    public static final String SQL_LIFECYCLE_ONLY = """
            er.RegistrationStatus NOT IN (
                N'Draft', N'Pending', N'Approved', N'Rejected',
                N'Chờ duyệt', N'Duyệt', N'Loại',
                N'RegistrationRejected', N'Cancelled'
            )
            """;

    /** SQL: bỏ dòng ER hồ sơ gốc / xin duyệt hạng tài liệu. */
    public static final String SQL_EXCLUDE_PROFILE_DOC =
            "(er.Notes IS NULL OR ("
                    + "er.Notes NOT LIKE N'%#PROFILE_DOC#%'"
                    + " AND er.Notes NOT LIKE N'%#LICENCE_DOC#%'"
                    + "))";

    /** Nối an toàn sau WHERE/AND — tránh lỗi text-block dính `ANDer` / `WHEREprof`. */
    public static final String SQL_AND_LIFECYCLE_ONLY =
            " AND " + SQL_LIFECYCLE_ONLY.stripLeading();

    public static final String SQL_AND_EXCLUDE_PROFILE_DOC =
            " AND " + SQL_EXCLUDE_PROFILE_DOC;

    public static final String SQL_ACTIVE_EXAM_REGISTRATION_FILTER =
            SQL_AND_LIFECYCLE_ONLY + SQL_AND_EXCLUDE_PROFILE_DOC;

    private ExamRegistrationLifecycleStatus() {
    }

    /** True nếu ER đang ở trạng thái CancelRequested (chỉ đọc/hiển thị). */
    public static boolean isCancellationPending(String status) {
        return CANCEL_REQUESTED.equalsIgnoreCase(status);
    }

    /** Đổi mã lifecycle DB sang nhãn tiếng Việt cho UI. */
    public static String toDisplayLabel(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return switch (status.trim()) {
            case PRE_REGISTERED -> "Chờ xét duyệt";
            case CANCEL_REQUESTED -> "Chờ hủy đăng ký";
            case REGISTRATION_REJECTED -> "Bị từ chối";
            case CANCELLED -> "Đã hủy";
            case CHECKED_IN, PRESENT -> "Đã check-in";
            case COMPLETED -> "Đã hoàn thành";
            case WALK_IN -> "Đăng ký tại quầy";
            default -> status;
        };
    }
}
