package examstaff.controller;

/**
 * Tên khóa session exam staff (Presentation).
 * <p>
 * Giá trị chuỗi phải khớp JSP / code cũ — chỉ đổi chỗ gọi, không đổi tên key.
 * <ul>
 *   <li>{@link #SELECTED_EXAM_ID} — kỳ đang chọn (URL/session commit)</li>
 *   <li>{@link #LOADED_EXAM_ID} — kỳ đã load queue lần gần nhất</li>
 *   <li>{@link #LAST_LOADED_EXAM_ID} — kỳ đã load (đồng bộ board/cache)</li>
 *   <li>{@link #CALLING_SBD} — SBD đang gọi / đang ở bàn thủ tục</li>
 *   <li>{@link #SHIFT_PAUSED}/{@link #SHIFT_ENDED} — cờ ca ("true")</li>
 * </ul>
 */
public final class ExamStaffSessionKeys {

    public static final String SELECTED_EXAM_ID = "selectedExamId";
    public static final String LOADED_EXAM_ID = "examStaffLoadedExamId";
    public static final String LAST_LOADED_EXAM_ID = "lastLoadedExamId";

    public static final String CANDIDATE_QUEUE = "candidateQueue";
    public static final String ACTIVE_CALL_QUEUE = "activeCallQueue";
    public static final String PROCEDURE_DONE_CANDIDATES = "procedureDoneCandidates";
    public static final String CALL_QUEUE_ORDER = "callQueueOrder";
    public static final String CALL_QUEUE_ORDER_EXAM_ID = "callQueueOrderExamId";

    public static final String CALLING_SBD = "callingSbd";
    public static final String LAST_SELECTED_SBD = "lastSelectedSbd";
    public static final String PERMANENT_ABSENTS = "permanentAbsents";

    public static final String PROCEDURE_STEP = "procedureStep";
    public static final String PROCEDURE_JUST_PAID = "procedureJustPaid";
    public static final String PROCEDURE_JUST_PAID_SBD = "procedureJustPaidSbd";

    public static final String SHIFT_PAUSED = "shiftPaused";
    public static final String SHIFT_ENDED = "shiftEnded";

    public static final String EXAM_CONTROL_MSG = "examControlMsg";
    public static final String EXAM_CONTROL_ERROR = "examControlError";

    public static final String FLAG_TRUE = "true";

    private ExamStaffSessionKeys() {
    }
}
