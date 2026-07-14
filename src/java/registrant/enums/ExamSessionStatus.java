package registrant.enums;

public enum ExamSessionStatus {
    SCHEDULED("Scheduled"),
    OPEN("Open"),
    IN_PROGRESS("InProgress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NOT_STARTED_VN("Chưa diễn ra"),
    IN_PROGRESS_VN("Đang diễn ra");

    private final String status;

    ExamSessionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static boolean canStartSession(String status) {
        if (status == null) {
            return false;
        }
        return SCHEDULED.status.equalsIgnoreCase(status)
                || OPEN.status.equalsIgnoreCase(status)
                || NOT_STARTED_VN.status.equalsIgnoreCase(status)
                || IN_PROGRESS_VN.status.equalsIgnoreCase(status);
    }

    public static boolean isSessionInProgress(String status) {
        if (status == null) {
            return false;
        }
        return IN_PROGRESS.status.equalsIgnoreCase(status)
                || IN_PROGRESS_VN.status.equalsIgnoreCase(status);
    }

    public static boolean isSessionEnded(String status) {
        return COMPLETED.status.equalsIgnoreCase(status) || CANCELLED.status.equalsIgnoreCase(status);
    }
}
