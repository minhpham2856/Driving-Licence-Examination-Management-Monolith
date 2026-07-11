package dao;

/** ServletContext attribute keys for {@link CallBoardDAO} implementations. */
public final class CallBoardAttributeKeys {
    public static final String BOARDS_MAP = "candidateCallBoards";
    /** Key mới. */
    public static final String ACTIVE_EXAM_ID = "activeCallExamId";
    /** Legacy — dual-read khi migrate runtime. */
    public static final String ACTIVE_SESSION_ID = "activeCallSessionId";

    private CallBoardAttributeKeys() {
    }
}
