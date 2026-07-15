package examstaff.service.impl.support.call;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CallBoardState;

import java.util.List;

/** Đồng bộ trạng thái bảng gọi - logic nghiệp vụ thuần. */
public final class CallBoardRules {

    /** Utility class — không khởi tạo. */
    private CallBoardRules() {
    }

    /**
     * Đồng bộ CallBoard theo SBD đang gọi / hàng đợi / trạng thái ca.
     * Giữ deskBusy hiện tại; chỉ cập nhật calling khi bàn trống.
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
