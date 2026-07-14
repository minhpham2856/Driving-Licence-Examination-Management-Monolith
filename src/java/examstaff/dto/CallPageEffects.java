package examstaff.dto;

/**
 * Side-effect trình bày sau {@code preparePage} (session + CallBoard).
 * Gom cờ để servlet apply một đoạn thay vì rải 10 boolean.
 * <p>
 * Không chứa nghiệp vụ — chỉ mang quyết định đã tính trong BLL.
 */
public class CallPageEffects {

    private String callingSbd;
    private boolean clearCallingSbd;
    private boolean shiftEnded;
    private boolean shiftPaused;
    private boolean pauseBoard;
    private boolean clearProcedureJustPaidSbd;
    private boolean persistQueueOrder;
    private boolean releaseDesk;
    private String releaseDeskCallingSbd;
    private boolean syncBoard;
    private String boardCallingSbd;
    private int publishExamId;

    public static CallPageEffects fromView(CandidateCallPageViewDTO view) {
        CallPageEffects e = new CallPageEffects();
        if (view == null) {
            return e;
        }
        e.callingSbd = view.getCallingSbd();
        e.clearCallingSbd = view.isClearCallingSbd();
        e.shiftEnded = view.isShiftEnded();
        e.shiftPaused = view.isShiftPaused();
        e.pauseBoard = view.isPauseBoard();
        e.clearProcedureJustPaidSbd = view.isClearProcedureJustPaidSbd();
        e.persistQueueOrder = view.isPersistQueueOrder();
        e.releaseDesk = view.isReleaseDesk();
        e.releaseDeskCallingSbd = view.getReleaseDeskCallingSbd();
        e.syncBoard = view.isSyncBoard();
        e.boardCallingSbd = view.getBoardCallingSbd();
        e.publishExamId = view.getPublishExamId();
        return e;
    }

    public String getCallingSbd() {
        return callingSbd;
    }

    public boolean isClearCallingSbd() {
        return clearCallingSbd;
    }

    public boolean isShiftEnded() {
        return shiftEnded;
    }

    public boolean isShiftPaused() {
        return shiftPaused;
    }

    public boolean isPauseBoard() {
        return pauseBoard;
    }

    public boolean isClearProcedureJustPaidSbd() {
        return clearProcedureJustPaidSbd;
    }

    public boolean isPersistQueueOrder() {
        return persistQueueOrder;
    }

    public boolean isReleaseDesk() {
        return releaseDesk;
    }

    public String getReleaseDeskCallingSbd() {
        return releaseDeskCallingSbd;
    }

    public boolean isSyncBoard() {
        return syncBoard;
    }

    public String getBoardCallingSbd() {
        return boardCallingSbd;
    }

    public int getPublishExamId() {
        return publishExamId;
    }
}
