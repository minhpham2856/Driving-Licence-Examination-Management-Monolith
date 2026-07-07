package dto.payload;

import util.SessionShiftLabels;

public class SessionControlData {

    private final boolean morningSession;
    private final int examinerCount;

    public SessionControlData(boolean morningSession, int examinerCount) {
        this.morningSession = morningSession;
        this.examinerCount = examinerCount;
    }

    public boolean isMorningSession() {
        return morningSession;
    }

    public String getShiftLabel() {
        return SessionShiftLabels.toLabel(morningSession);
    }

    public int getExaminerCount() {
        return examinerCount;
    }
}
