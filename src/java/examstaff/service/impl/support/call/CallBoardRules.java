package examstaff.service.impl.support.call;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CallBoardState;

import java.util.List;

/**
 * Luật nghiệp vụ thuần (pure) để mutate {@link CallBoardState}.
 * <p>
 * <b>Không</b> gọi DAO / SQL / Servlet — chỉ nhận state hiện tại + tham số, trả state mới.
 * {@code StaffCallServiceImpl} chịu trách nhiệm: {@code getState} → rules → {@code saveState}.
 *
 * Các thao tác và khi nào dùng:
 * - {@link #syncBoard} — sau khi gọi số / đổi calling / end ca: cập nhật calling + next + queue order;
 *       <b>không ghi đè calling</b> nếu bàn thủ tục đang bận ({@code deskBusy})
 * - {@link #occupyDesk} — thí sinh vào bàn thủ tục: {@code deskBusy=true}, gắn {@code deskSbd}
 * - {@link #releaseDeskAndCall} — xong thủ tục: clear desk, đặt calling mới, resolve next
 * - {@link #pauseBoard} — tạm dừng gọi: clear calling/desk, {@code examPaused=true}, giữ thứ tự queue
 * - {@link #resolveNextSbd} — chỉ đọc: suy SBD kế từ board + queue (không mutate)
 *
 * Quan hệ với hàng đợi DB:
 * {@code nextSbd} và {@code queueOrderSbds} lấy từ list {@link ExamRegistrationDTO}
 * qua {@link CallQueueRules} — board chỉ cache thứ tự để TV/staff đồng bộ nhanh;
 * danh sách thí sinh “thật” vẫn đến từ DB khi load snapshot/queue.
 */
public final class CallBoardRules {

    /** Utility class — không khởi tạo. */
    private CallBoardRules() {
    }

    /**
     * Đồng bộ CallBoard theo SBD đang gọi / hàng đợi / trạng thái ca.
     * <p><b>Luồng bên trong:</b>
     * - Tạo state mới nếu {@code current == null}
     * - Nhớ {@code deskBusy}/{@code deskSbd} hiện tại
     * - Set {@code examId}; chỉ set {@code callingSbd} khi bàn <b>không</b> bận
     *       (tránh TV nhảy SBD trong lúc đang làm thủ tục)
     * - Resolve {@code nextSbd}: nếu desk bận thì “sau deskSbd”, không thì “sau calling”
     * - Ghi {@code queueOrderSbds}, {@code shiftEnded}; end ca thì clear pause
     * - Khôi phục desk flags + stamp {@code updatedAtMs} (poll TV dùng để biết có đổi)
     * @param current    trạng thái board hiện tại (null → tạo mới)
     * @param examId     mã kỳ thi trên board
     * @param callingSbd SBD đang gọi (có thể blank)
     * @param queue      hàng đợi để resolve nextSbd và thứ tự
     * @param shiftEnded ca đã đóng hay chưa
     * @return board đã cập nhật (cùng instance nếu current khác null)
     */
    public static CallBoardState syncBoard(CallBoardState current, int examId,
            String callingSbd, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Tải / khởi tạo state
        CallBoardState state = current != null ? current : new CallBoardState();
        boolean wasDeskBusy = state.isDeskBusy();
        String wasDeskSbd = state.getDeskSbd();

        // Mutate: kỳ thi + calling (chỉ khi bàn không bận)
        state.setExamId(examId);
        if (!wasDeskBusy) {
            state.setCallingSbd(emptyToNull(callingSbd));
        }
        // Mutate: nextSbd theo desk hoặc theo calling
        if (wasDeskBusy && wasDeskSbd != null && !wasDeskSbd.isBlank()) {
            state.setNextSbd(CallQueueRules.resolveNextCallingSbd(queue, wasDeskSbd));
        } else {
            state.setNextSbd(CallQueueRules.resolveNextCallingSbd(queue, state.getCallingSbd()));
        }
        state.setQueueOrderSbds(CallQueueRules.extractSbdOrder(queue));
        state.setShiftEnded(shiftEnded);
        if (shiftEnded) {
            state.setExamPaused(false);
        }
        // Giữ desk + timestamp
        state.setDeskBusy(wasDeskBusy);
        state.setDeskSbd(wasDeskSbd);
        state.setUpdatedAtMs(System.currentTimeMillis());
        return state;
    }

    /**
     * Chiếm bàn thủ tục: deskBusy=true, gắn deskSbd, resolve next.
     * <p>
     * Gọi từ {@code ProcedureServlet} khi thí sinh bắt đầu làm thủ tục tại desk.
     * Nếu chưa có {@code callingSbd}, gán bằng {@code deskSbd} để TV vẫn hiện SBD đang xử lý.
     * @param current    trạng thái board hiện tại (null → tạo mới)
     * @param examId     mã kỳ thi
     * @param deskSbd    SBD tại bàn (bắt buộc)
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng hay chưa
     * @return board đã cập nhật, hoặc {@code current} nếu deskSbd rỗng
     */
    public static CallBoardState occupyDesk(CallBoardState current, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Validate
        if (deskSbd == null || deskSbd.isBlank()) {
            return current;
        }
        // Load
        CallBoardState state = current != null ? current : new CallBoardState();
        // Mutate
        state.setExamId(examId);
        state.setDeskBusy(true);
        state.setDeskSbd(emptyToNull(deskSbd));
        if (state.getCallingSbd() == null || state.getCallingSbd().isBlank()) {
            state.setCallingSbd(state.getDeskSbd());
        }
        state.setNextSbd(CallQueueRules.resolveNextCallingSbd(queue, deskSbd));
        state.setQueueOrderSbds(CallQueueRules.extractSbdOrder(queue));
        state.setShiftEnded(shiftEnded);
        if (shiftEnded) {
            state.setExamPaused(false);
        }
        state.setUpdatedAtMs(System.currentTimeMillis());
        return state;
    }

    /**
     * Tạm dừng gọi thí sinh — giữ thứ tự hàng đợi, không đánh vắng.
     * <p>
     * Clear calling/desk trên board; {@code examPaused=true} để Public Call hiện trạng thái pause.
     * Khác với pause kỳ thi trên DB ({@code ExamControlServlet}) — đây chỉ pause bảng gọi runtime.
     * @param current trạng thái board hiện tại (null → tạo mới)
     * @param examId  mã kỳ thi
     * @param queue   hàng đợi (để giữ queueOrderSbds)
     * @return board đã đánh paused, clear calling/desk
     */
    public static CallBoardState pauseBoard(CallBoardState current, int examId,
            List<ExamRegistrationDTO> queue) {
        // Load
        CallBoardState state = current != null ? current : new CallBoardState();
        // Mutate: clear calling/desk, đánh paused
        state.setExamId(examId);
        state.setCallingSbd(null);
        state.setNextSbd(null);
        state.setDeskBusy(false);
        state.setDeskSbd(null);
        state.setShiftEnded(false);
        state.setExamPaused(true);
        if (queue != null && !queue.isEmpty()) {
            state.setQueueOrderSbds(CallQueueRules.extractSbdOrder(queue));
        }
        state.setUpdatedAtMs(System.currentTimeMillis());
        return state;
    }

    /**
     * Giải phóng bàn và chuyển callingSbd mới (sau khi thủ tục xong).
     * <p>
     * Pattern: {@code deskBusy=false}, {@code deskSbd=null}, gắn calling mới, resolve next
     * từ queue. Nếu đang gọi lại một SBD (không end ca) thì clear {@code examPaused}.
     * @param current    trạng thái board hiện tại (null → tạo mới)
     * @param examId     mã kỳ thi
     * @param callingSbd SBD gọi tiếp theo (có thể blank)
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng hay chưa
     * @return board đã clear desk và cập nhật calling/next
     */
    public static CallBoardState releaseDeskAndCall(CallBoardState current, int examId,
            String callingSbd, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        // Load
        CallBoardState state = current != null ? current : new CallBoardState();
        // Mutate: giải phóng desk + gắn calling
        state.setExamId(examId);
        state.setDeskBusy(false);
        state.setDeskSbd(null);
        state.setCallingSbd(emptyToNull(callingSbd));
        state.setNextSbd(CallQueueRules.resolveNextCallingSbd(queue, state.getCallingSbd()));
        state.setQueueOrderSbds(CallQueueRules.extractSbdOrder(queue));
        state.setShiftEnded(shiftEnded);
        if (shiftEnded) {
            state.setExamPaused(false);
        } else if (callingSbd != null && !callingSbd.isBlank()) {
            state.setExamPaused(false);
        }
        state.setUpdatedAtMs(System.currentTimeMillis());
        return state;
    }

    /**
     * Suy SBD kế tiếp từ board (ưu tiên desk → nextSbd lưu sẵn → calling).
     * Chỉ đọc — không mutate board.
     * @param board trạng thái bảng gọi (null = lấy đầu pending của queue)
     * @param queue hàng đợi đầy đủ
     * @return SBD kế tiếp hoặc null
     */
    public static String resolveNextSbd(CallBoardState board, List<ExamRegistrationDTO> queue) {
        // Validate / nhánh không có board
        if (board == null) {
            return CallQueueRules.resolveNextCallingSbd(queue, null);
        }
        // Ưu tiên theo desk đang bận
        if (board.isDeskBusy() && board.getDeskSbd() != null && !board.getDeskSbd().isBlank()) {
            return CallQueueRules.resolveNextCallingSbd(queue, board.getDeskSbd());
        }
        // Dùng nextSbd đã cache trên board
        if (board.getNextSbd() != null && !board.getNextSbd().isBlank()) {
            return board.getNextSbd();
        }
        return CallQueueRules.resolveNextCallingSbd(queue, board.getCallingSbd());
    }

    /**
     * Chuẩn hóa chuỗi rỗng / blank thành null; trim nếu còn nội dung.
     * @param value chuỗi đầu vào
     * @return null nếu blank, ngược lại đã trim
     */
    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
