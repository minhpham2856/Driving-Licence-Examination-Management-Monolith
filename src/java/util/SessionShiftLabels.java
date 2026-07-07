package util;

public final class SessionShiftLabels {

    public static final String MORNING = "Ca sáng";
    public static final String AFTERNOON = "Ca chiều";

    private SessionShiftLabels() {
    }

    public static String toLabel(boolean morningSession) {
        return morningSession ? MORNING : AFTERNOON;
    }
}
