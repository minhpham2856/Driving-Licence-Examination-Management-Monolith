package registrant.enums;

/**
 * Trạng thái {@code ExamRegistration} cho luồng đăng ký ca thi (tách khỏi workflow tài liệu Draft/Pending/Approved/Rejected).
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

    /** Cho phép đăng ký lại cùng phần thi + hạng khi đăng ký trước đã bị từ chối hoặc đã hủy. */
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
