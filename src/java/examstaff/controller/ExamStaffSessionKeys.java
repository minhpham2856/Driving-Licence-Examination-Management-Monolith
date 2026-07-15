package examstaff.controller;

import shared.Attributes;

/**
 * Alias session keys exam staff — giá trị khớp {@link Attributes.ExamStaff} / JSP.
 * <p>
 * Nhóm key (không có method nghiệp vụ; chỉ hằng số):
 * <ul>
 *   <li><b>Kỳ thi đã chọn/load:</b> {@link #SELECTED_EXAM_ID}, {@link #LOADED_EXAM_ID},
 *       {@link #LAST_LOADED_EXAM_ID}</li>
 *   <li><b>Hàng đợi thí sinh:</b> {@link #CANDIDATE_QUEUE}, {@link #ACTIVE_CALL_QUEUE},
 *       {@link #PROCEDURE_DONE_CANDIDATES}, {@link #CALL_QUEUE_ORDER},
 *       {@link #CALL_QUEUE_ORDER_EXAM_ID}</li>
 *   <li><b>Gọi số / vắng:</b> {@link #CALLING_SBD}, {@link #LAST_SELECTED_SBD},
 *       {@link #PERMANENT_ABSENTS}</li>
 *   <li><b>Bàn thủ tục:</b> {@link #PROCEDURE_STEP}, {@link #PROCEDURE_JUST_PAID},
 *       {@link #PROCEDURE_JUST_PAID_SBD}</li>
 *   <li><b>Ca thi:</b> {@link #SHIFT_PAUSED}, {@link #SHIFT_ENDED}</li>
 *   <li><b>Flash exam-control:</b> {@link #EXAM_CONTROL_MSG}, {@link #EXAM_CONTROL_ERROR}</li>
 *   <li><b>Cờ boolean dạng chuỗi:</b> {@link #FLAG_TRUE} ({@code "true"})</li>
 * </ul>
 */
public final class ExamStaffSessionKeys {

    /** Mã kỳ thi đang chọn trên sidebar/picker. */
    public static final String SELECTED_EXAM_ID = Attributes.ExamStaff.SELECTED_EXAM_ID;
    /** Mã kỳ đã load queue vào session lần gần nhất. */
    public static final String LOADED_EXAM_ID = Attributes.ExamStaff.LOADED_EXAM_ID;
    /** Mã kỳ load cuối (dùng so sánh đổi kỳ / cache-buster UI). */
    public static final String LAST_LOADED_EXAM_ID = Attributes.ExamStaff.LAST_LOADED_EXAM_ID;

    /** Queue đầy đủ thí sinh của kỳ. */
    public static final String CANDIDATE_QUEUE = Attributes.ExamStaff.CANDIDATE_QUEUE;
    /** Queue còn gọi được (chưa xong thủ tục / chưa nghỉ hẳn). */
    public static final String ACTIVE_CALL_QUEUE = Attributes.ExamStaff.ACTIVE_CALL_QUEUE;
    /** Thí sinh đã hoàn tất thủ tục. */
    public static final String PROCEDURE_DONE_CANDIDATES = Attributes.ExamStaff.PROCEDURE_DONE_CANDIDATES;
    /** Thứ tự SBD khi gọi số. */
    public static final String CALL_QUEUE_ORDER = Attributes.ExamStaff.CALL_QUEUE_ORDER;
    /** Kỳ thi gắn với {@link #CALL_QUEUE_ORDER}. */
    public static final String CALL_QUEUE_ORDER_EXAM_ID = Attributes.ExamStaff.CALL_QUEUE_ORDER_EXAM_ID;

    /** SBD đang được gọi / mở bàn. */
    public static final String CALLING_SBD = Attributes.ExamStaff.CALLING_SBD;
    /** SBD chọn gần nhất trên bàn thủ tục (phát hiện đổi thí sinh). */
    public static final String LAST_SELECTED_SBD = Attributes.ExamStaff.LAST_SELECTED_SBD;
    /** Danh sách vắng mặt vĩnh viễn trong ca. */
    public static final String PERMANENT_ABSENTS = Attributes.ExamStaff.PERMANENT_ABSENTS;

    /** Bước wizard thủ tục (1 lý lịch / 2 ảnh / 3 phí). */
    public static final String PROCEDURE_STEP = Attributes.ExamStaff.PROCEDURE_STEP;
    /** Cờ vừa thu phí thành công. */
    public static final String PROCEDURE_JUST_PAID = Attributes.ExamStaff.PROCEDURE_JUST_PAID;
    /** SBD vừa thu phí (hiển thị xác nhận / in). */
    public static final String PROCEDURE_JUST_PAID_SBD = Attributes.ExamStaff.PROCEDURE_JUST_PAID_SBD;

    /** Ca đang tạm dừng. */
    public static final String SHIFT_PAUSED = Attributes.ExamStaff.SHIFT_PAUSED;
    /** Ca đã kết thúc. */
    public static final String SHIFT_ENDED = Attributes.ExamStaff.SHIFT_ENDED;

    /** Flash thông báo điều khiển kỳ thi (start/pause/…). */
    public static final String EXAM_CONTROL_MSG = Attributes.ExamStaff.EXAM_CONTROL_MSG;
    /** Flash lỗi điều khiển kỳ thi. */
    public static final String EXAM_CONTROL_ERROR = Attributes.ExamStaff.EXAM_CONTROL_ERROR;

    /** Giá trị cờ session dạng chuỗi {@code "true"}. */
    public static final String FLAG_TRUE = Attributes.ExamStaff.FLAG_TRUE;

    /** Không khởi tạo. */
    private ExamStaffSessionKeys() {
    }
}
