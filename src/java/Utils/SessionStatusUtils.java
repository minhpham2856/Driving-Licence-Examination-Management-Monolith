package Utils;

import Enums.SessionStatus;

public class SessionStatusUtils {

    public static boolean canStartSession(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String s = status.trim();
        return SessionStatus.SCHEDULED.getValue().equalsIgnoreCase(s) || SessionStatus.OPEN.getValue().equalsIgnoreCase(s);
    }

    public static boolean isSessionInProgress(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return SessionStatus.IN_PROGRESS.getValue().equalsIgnoreCase(status.trim());
    }

    public static boolean isSessionEnded(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String s = status.trim();
        return SessionStatus.COMPLETED.getValue().equalsIgnoreCase(s) || SessionStatus.CANCELLED.getValue().equalsIgnoreCase(s);
    }
}
