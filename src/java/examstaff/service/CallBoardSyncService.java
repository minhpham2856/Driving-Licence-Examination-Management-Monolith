package examstaff.service;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.view.CallBoardState;

import java.util.List;

/**
 * Đồng bộ trạng thái bảng gọi thí sinh (CallBoard) qua {@link CallBoardDAO}.
 * Không biết HTTP/ServletContext — DAO được truyền từ tầng Presentation.
 */
public interface CallBoardSyncService {

    /**
     * Đọc trạng thái bảng gọi của kỳ thi.
     *
     * @param callBoardDAO DAO lưu CallBoard (thường in-memory)
     * @param examId       mã kỳ thi
     * @return trạng thái hiện tại, hoặc null nếu chưa có
     */
    CallBoardState getState(CallBoardDAO callBoardDAO, int examId);

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
    void sync(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Đánh dấu bàn thủ tục đang bận với SBD đang làm thủ tục.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param deskSbd      SBD đang ở bàn
     * @param queue        hàng đợi
     * @param shiftEnded   true nếu ca gọi đã đóng
     */
    void occupyDesk(CallBoardDAO callBoardDAO, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Giải phóng bàn rồi gắn SBD đang gọi mới lên bảng (sau khi thủ tục xong).
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param callingSbd   SBD gọi tiếp theo (có thể null)
     * @param queue        hàng đợi
     * @param shiftEnded   true nếu ca gọi đã đóng
     */
    void releaseDeskAndCall(CallBoardDAO callBoardDAO, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded);

    /**
     * Tạm dừng ca gọi: xóa số đang gọi / bàn, giữ thứ tự hàng đợi, đánh dấu paused.
     *
     * @param callBoardDAO DAO bảng gọi
     * @param examId       mã kỳ thi
     * @param queue        hàng đợi (để ghi lại thứ tự SBD)
     */
    void pauseShift(CallBoardDAO callBoardDAO, int examId, List<ExamRegistrationDTO> queue);

    /**
     * Áp thứ tự SBD lưu trên bảng gọi lên danh sách hàng đợi trong bộ nhớ.
     *
     * @param queue hàng đợi gốc
     * @param board trạng thái bảng gọi (có {@code queueOrderSbds})
     * @return hàng đợi đã sắp theo board, hoặc queue gốc nếu board không có thứ tự
     */
    List<ExamRegistrationDTO> applyBoardOrder(List<ExamRegistrationDTO> queue, CallBoardState board);
}
