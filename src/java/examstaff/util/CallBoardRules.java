package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.CallBoardState;

import java.util.List;

/** Đồng bộ trạng thái bảng gọi — logic nghiệp vụ thuần (không HTTP/DAO). */
public final class CallBoardRules {

    private CallBoardRules() {
    }

    /**
     * Đồng bộ calling/next/queueOrder lên state mới.
     * Nếu bàn đang bận thì giữ nguyên {@code callingSbd} cũ, chỉ cập nhật next theo desk.
     *
     * @param current    state hiện tại (null = tạo mới)
     * @param examId     mã kỳ thi
     * @param callingSbd số đang gọi từ staff
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     * @return state đã cập nhật (mutates bản copy logic trên current)
     */
    public static CallBoardState syncBoard(CallBoardState current, int examId,
            String callingSbd, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState state = current != null ? current : new CallBoardState();
        boolean wasDeskBusy = state.isDeskBusy();
        String wasDeskSbd = state.getDeskSbd();

        state.setExamId(examId);
        if (!wasDeskBusy) {
            state.setCallingSbd(emptyToNull(callingSbd));
        }
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
        state.setDeskBusy(wasDeskBusy);
        state.setDeskSbd(wasDeskSbd);
        state.setUpdatedAtMs(System.currentTimeMillis());
        return state;
    }

    /**
     * Đánh dấu bàn thủ tục bận với {@code deskSbd}; next = thí sinh sau người ở bàn.
     *
     * @param current    state hiện tại
     * @param examId     mã kỳ thi
     * @param deskSbd    SBD đang ở bàn (blank → trả current không đổi)
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     * @return state sau khi occupy
     */
    public static CallBoardState occupyDesk(CallBoardState current, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (deskSbd == null || deskSbd.isBlank()) {
            return current;
        }
        CallBoardState state = current != null ? current : new CallBoardState();
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
     * @param current state hiện tại
     * @param examId  mã kỳ thi
     * @param queue   hàng đợi (để lưu queueOrderSbds)
     * @return state paused (calling/next/desk cleared)
     */
    public static CallBoardState pauseBoard(CallBoardState current, int examId,
            List<ExamRegistrationDTO> queue) {
        CallBoardState state = current != null ? current : new CallBoardState();
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
     * Giải phóng bàn rồi đặt SBD đang gọi mới.
     *
     * @param current    state hiện tại
     * @param examId     mã kỳ thi
     * @param callingSbd SBD gọi tiếp
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     * @return state sau khi release
     */
    public static CallBoardState releaseDeskAndCall(CallBoardState current, int examId,
            String callingSbd, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState state = current != null ? current : new CallBoardState();
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
     * Xác định SBD kế tiếp để hiển thị: ưu tiên next trên board, hoặc resolve từ queue.
     * Khi bàn bận thì next tính sau {@code deskSbd}.
     *
     * @param board trạng thái bảng gọi
     * @param queue hàng đợi
     * @return SBD kế tiếp hoặc null
     */
    public static String resolveNextSbd(CallBoardState board, List<ExamRegistrationDTO> queue) {
        if (board == null) {
            return CallQueueRules.resolveNextCallingSbd(queue, null);
        }
        if (board.isDeskBusy() && board.getDeskSbd() != null && !board.getDeskSbd().isBlank()) {
            return CallQueueRules.resolveNextCallingSbd(queue, board.getDeskSbd());
        }
        if (board.getNextSbd() != null && !board.getNextSbd().isBlank()) {
            return board.getNextSbd();
        }
        return CallQueueRules.resolveNextCallingSbd(queue, board.getCallingSbd());
    }

    /** Chuẩn hóa chuỗi rỗng / blank thành null. */
    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
