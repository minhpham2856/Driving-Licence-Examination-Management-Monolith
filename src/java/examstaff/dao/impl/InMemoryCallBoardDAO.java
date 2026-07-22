package examstaff.dao.impl;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai {@link CallBoardDAO} lưu {@link CallBoardState} trong bộ nhớ JVM (singleton).
 *
 * Vì sao dùng in-memory thay ServletContext?:
 * Call Board là trạng thái <b>runtime</b> (ai đang được gọi, bàn thủ tục bận, pause…),
 * không cần persist SQL. Approach 1 dùng một singleton JVM thay vì attribute trên
 *
 * {@code ServletContext}, nên:
 * - Không phụ thuộc Servlet API trong tầng persistence
 * - Mọi request (staff desk, TV public-call, JSON poll) đọc cùng một map
 * - Restart Tomcat / redeploy → mất state (giống hành vi cũ trên ServletContext)
 *
 * Cấu trúc dữ liệu:
 * <pre>
 *   INSTANCE
 *     ├── boards: ConcurrentHashMap&lt;examId, CallBoardState&gt;
 *     │     key   = ExamId (kỳ thi)
 *     │     value = snapshot calling/next/desk/queue/pause của kỳ đó
 *     └── activeExamId: Integer (kỳ đang chiếu trên màn Public Call)
 * </pre>
 *
 * Luồng đọc / ghi điển hình:
 * - Servlet lấy DAO qua {@code ExamStaffHttpSupport.callBoardDao(...)}
 * - Service: {@code getState} → mutate bằng {@code CallBoardRules} → {@code saveState}
 * - Public Call / API poll: {@code getState} + {@code getActiveExamId} → dựng snapshot JSON/JSP
 *
 * An toàn đồng thời (thread-safety):
 * - {@code ConcurrentHashMap} cho map boards — put/get theo examId an toàn giữa các request
 * - {@code volatile} cho {@code activeExamId} — đọc/ghi kỳ active thấy ngay giữa thread
 * - {@link #copy(CallBoardState)} khi get/save — caller không giữ reference nội bộ map,
 *       tránh race khi một thread sửa object đang đọc bởi thread khác
 * <p><b>Lưu ý cluster:</b> mỗi JVM node có bản copy riêng; không sync giữa các Tomcat instance.
 */
public final class InMemoryCallBoardDAO implements CallBoardDAO {

    /** Singleton duy nhất trong JVM process (eager init). */
    private static final InMemoryCallBoardDAO INSTANCE = new InMemoryCallBoardDAO();

    /**
     * Map trạng thái board theo kỳ thi.
     * Key = examId &gt; 0; value = bản copy đã lưu lần save gần nhất.
     */
    private final Map<Integer, CallBoardState> boards = new ConcurrentHashMap<>();

    /**
     * Kỳ thi đang active trên màn Public Call (TV).
     * {@code null} hoặc ≤ 0 nghĩa là chưa chọn kỳ chiếu.
     * Dùng {@code volatile} vì nhiều request (poll API) đọc đồng thời với staff ghi.
     */
    private volatile Integer activeExamId;

    /** Chỉ tạo qua {@link #INSTANCE}; không cho {@code new} từ bên ngoài. */
    private InMemoryCallBoardDAO() {
    }

    /**
     * Điểm vào duy nhất — mọi servlet/support đều dùng cùng instance này.
     * @return repository Call Board dùng chung toàn ứng dụng
     */
    public static InMemoryCallBoardDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Đọc board của một kỳ.
     * <p>
     * Trả về <b>bản copy</b> (không phải object trong map) để service có thể mutate
     * rồi {@link #saveState} mà không làm hỏng bản đang lưu giữa chừng.
     * @param examId mã kỳ thi
     * @return copy {@link CallBoardState}, hoặc {@code null} nếu kỳ chưa từng được save
     */
    @Override
    public CallBoardState getState(int examId) {
        CallBoardState state = boards.get(examId);
        return state != null ? copy(state) : null;
    }

    /**
     * Ghi đè board của kỳ {@code examId}.
     * <p>
     * Cũng lưu <b>bản copy</b> vào map — sau khi return, thay đổi trên {@code state}
     * của caller không ảnh hưởng bản đã lưu (phải gọi save lại).
     * @param examId mã kỳ thi
     * @param state  trạng thái mới; {@code null} → bỏ qua (không xóa entry cũ)
     */
    @Override
    public void saveState(int examId, CallBoardState state) {
        if (state == null) {
            return;
        }
        boards.put(examId, copy(state));
    }

    /**
     * Đánh dấu kỳ đang chiếu trên Public Call (sau sync/occupy/release/pause).
     * Staff gọi số / thủ tục thường set luôn khi lưu board để TV biết kỳ nào đang live.
     * @param examId mã kỳ &gt; 0 để set; ≤ 0 → clear ({@code null})
     */
    @Override
    public void setActiveExamId(int examId) {
        activeExamId = examId > 0 ? examId : null;
    }

    /**
     * Kỳ đang active cho Public Call khi URL/session không chỉ rõ {@code examId}.
     * @return examId dương, hoặc {@code null} nếu chưa set / đã clear
     */
    @Override
    public Integer getActiveExamId() {
        Integer id = activeExamId;
        return id != null && id > 0 ? id : null;
    }

    /**
     * Deep-ish copy field-by-field.
     * {@code queueOrderSbds} được copy lại trong {@link CallBoardState#setQueueOrderSbds}
     * (ArrayList mới), nên list trong map độc lập với list của caller.
     */
    private static CallBoardState copy(CallBoardState source) {
        CallBoardState copy = new CallBoardState();
        copy.setExamId(source.getExamId());
        copy.setCallingSbd(source.getCallingSbd());
        copy.setNextSbd(source.getNextSbd());
        copy.setShiftEnded(source.isShiftEnded());
        copy.setExamPaused(source.isExamPaused());
        copy.setUpdatedAtMs(source.getUpdatedAtMs());
        copy.setQueueOrderSbds(source.getQueueOrderSbds());
        copy.setDeskBusy(source.isDeskBusy());
        copy.setDeskSbd(source.getDeskSbd());
        return copy;
    }
}
