package examstaff.dao;

import examstaff.dto.CallBoardState;

/**
 * Cổng truy cập trạng thái bảng gọi thí sinh <b>runtime</b> (Call Board).
 *
 * Vai trò trong kiến trúc:
 * Call Board <b>không</b> lưu SQL — chỉ giữ trạng thái đang diễn ra của ca gọi số
 * (SBD đang gọi, bàn thủ tục, pause, thứ tự queue) để staff desk và màn Public Call
 * (TV / JSON poll) dùng chung một nguồn sự thật.
 * <pre>
 *   CandidateCallServlet / ProcedureServlet / ExamControlServlet
 *            │  syncBoard / occupyDesk / releaseDesk / pause / resume
 *            ▼
 *      StaffCallService  ──mutate──►  CallBoardRules (pure)
 *            │
 *            ▼  getState / saveState / setActiveExamId
 *      CallBoardDAO  ◄── InMemoryCallBoardDAO (singleton JVM)
 *            ▲
 *            │  getState / getActiveExamId
 *   PublicCallServlet / PublicCallStateServlet (/api/public-call/state)
 * </pre>
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.InMemoryCallBoardDAO} — map in-memory theo {@code examId},
 * không ghi DB. Lấy instance qua {@code ExamStaffHttpSupport.callBoardDao(...)}.
 *
 * Hợp đồng copy:
 * {@link #getState} / {@link #saveState} nên trả / lưu bản copy để tránh hai thread
 * cùng sửa một object trong storage.
 */
public interface CallBoardDAO {

    /**
     * Lấy trạng thái bảng gọi theo kỳ thi.
     * <p>
     * Triển khai in-memory: đọc map {@code boards[examId]}, trả bản copy an toàn.
     * {@code null} = kỳ chưa từng sync (Public Call sẽ dựng snapshot “trống / từ queue DB”).
     * @param examId mã kỳ thi cần lấy trạng thái bảng gọi
     * @return bản copy {@link CallBoardState} nếu đã khởi tạo; {@code null} nếu chưa có
     */
    CallBoardState getState(int examId);

    /**
     * Lưu / ghi đè trạng thái bảng gọi của kỳ thi.
     * <p>
     * Pattern service: {@code getState} → {@code CallBoardRules.*} → {@code saveState}.
     * {@code state == null} thì không cập nhật (không xóa board cũ).
     * @param examId mã kỳ thi cần lưu trạng thái
     * @param state  trạng thái mới của bảng gọi; {@code null} thì không cập nhật
     */
    void saveState(int examId, CallBoardState state);

    /**
     * Đặt kỳ thi đang active trên bảng gọi công khai (Public Call).
     * <p>
     * Khi staff sync/occupy/release/pause, service thường gọi luôn method này
     * để TV biết kỳ nào đang live nếu URL không có {@code examId}.
     * @param examId mã kỳ thi đang chiếu / gọi thí sinh công khai
     */
    void setActiveExamId(int examId);

    /**
     * Đọc kỳ thi đang active cho màn hình Public Call.
     * <p>
     * Dùng trong {@code resolveActiveExamId(urlExamId, sessionExamId, boardActiveExamId)}
     * khi URL và session đều không chỉ rõ kỳ.
     * @return {@code examId} &gt; 0 nếu đã set; {@code null} nếu chưa đặt kỳ active
     */
    Integer getActiveExamId();
}
