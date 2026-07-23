package registrant.enums;

/** Trạng thái ER đăng ký đợt thi (portal) — tách khỏi workflow tài liệu Draft/Pending/Approved/Rejected. */
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

    /** SQL: bỏ dòng ER đánh dấu hồ sơ gốc (#PROFILE_DOC#). */
    public static final String SQL_EXCLUDE_PROFILE_DOC =
            "(er.Notes IS NULL OR er.Notes NOT LIKE N'%#PROFILE_DOC#%')";

    private ExamRegistrationLifecycleStatus() {
    }

    public static boolean isDocumentWorkflowStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return ProfileRegistrationStatus.DRAFT.equalsIgnoreCase(status)
                || ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)
                || ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)
                || ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status);
    }

    public static boolean allowsRepeatRegistration(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return REGISTRATION_REJECTED.equalsIgnoreCase(status)
                || CANCELLED.equalsIgnoreCase(status);
    }

    public static boolean blocksNewRegistrationForSection(String status) {
        if (status == null || status.isBlank() || isDocumentWorkflowStatus(status)) {
            return false;
        }
        return !allowsRepeatRegistration(status);
    }

    public static boolean canRequestCancellation(String status, boolean sbdPending) {
        if (!sbdPending || status == null) {
            return false;
        }
        return PRE_REGISTERED.equalsIgnoreCase(status);
    }

    public static boolean isCancellationPending(String status) {
        return CANCEL_REQUESTED.equalsIgnoreCase(status);
    }

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
