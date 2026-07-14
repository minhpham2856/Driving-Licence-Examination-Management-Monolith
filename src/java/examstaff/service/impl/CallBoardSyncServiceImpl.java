package examstaff.service.impl;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CallBoardSyncService;
import examstaff.util.CallBoardRules;
import examstaff.util.CallQueueRules;

import java.util.List;

/** Implementation: áp dụng {@link CallBoardRules} rồi persist qua {@link CallBoardDAO}. */
public class CallBoardSyncServiceImpl implements CallBoardSyncService {

    /**
     * Đọc trạng thái bảng gọi của kỳ thi.
     *
     * @param callBoardDAO DAO lưu CallBoard (thường in-memory)
     * @param examId       mã kỳ thi
     * @return trạng thái hiện tại, hoặc null nếu chưa có
     */
    @Override
    public CallBoardState getState(CallBoardDAO callBoardDAO, int examId) {
        return callBoardDAO.getState(examId);
    }

    /**
     * Đồng bộ số đang gọi / số kế tiếp / thứ tự hàng đợi lên bảng gọi.
     * Khi bàn đang bận ({@code deskBusy}) thì không đè {@code callingSbd}.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD đang gọi (có thể null)
     * @param queue        hàng đợi đầy đủ sau khi sắp xếp
     * @param shiftEnded   true nếu ca gọi đã đóng
     */
    @Override
    public void sync(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.syncBoard(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Đánh dấu bàn thủ tục đang bận với SBD đang làm thủ tục.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param deskSbd      SBD đang ở bàn
     * @param queue        hàng đợi
     * @param shiftEnded   true nếu ca gọi đã đóng
     */
    @Override
    public void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (examId <= 0 || deskSbd == null || deskSbd.isBlank()) {
            return;
        }
        CallBoardState updated = CallBoardRules.occupyDesk(
                callBoardDAO.getState(examId), examId, deskSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Giải phóng bàn rồi gắn SBD đang gọi mới lên bảng (sau khi thủ tục xong).
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD gọi tiếp theo (có thể null)
     * @param queue        hàng đợi
     * @param shiftEnded   true nếu ca gọi đã đóng
     */
    @Override
    public void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        CallBoardState updated = CallBoardRules.releaseDeskAndCall(
                callBoardDAO.getState(examId), examId, callingSbd, queue, shiftEnded);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Tạm dừng ca gọi: xóa số đang gọi / bàn, giữ thứ tự hàng đợi, đánh dấu paused.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param queue        hàng đợi (để ghi lại thứ tự SBD)
     */
    @Override
    public void pauseShift(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue) {
        CallBoardState updated = CallBoardRules.pauseBoard(
                callBoardDAO.getState(examId), examId, queue);
        callBoardDAO.saveState(examId, updated);
        callBoardDAO.setActiveExamId(examId);
    }

    /**
     * Áp thứ tự SBD lưu trên bảng gọi lên danh sách hàng đợi trong bộ nhớ.
     *
     * @param queue hàng đợi gốc
     * @param board trạng thái bảng gọi (có {@code queueOrderSbds})
     * @return hàng đợi đã sắp theo board, hoặc queue gốc nếu board không có thứ tự
     */
    @Override
    public List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board) {
        if (board == null || board.getQueueOrderSbds() == null || board.getQueueOrderSbds().isEmpty()) {
            return queue;
        }
        return CallQueueRules.applyQueueOrder(queue, board.getQueueOrderSbds());
    }
}
