package examstaff.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Trạng thái runtime bảng gọi thí sinh (in-memory) — nguồn sự thật chia sẻ giữa staff desk và Public Call.
 *
 * Vai trò trong luồng examstaff:
 * Lưu calling / next / thứ tự queue / desk busy / pause / end theo từng {@code examId}.
 * Không phụ thuộc Servlet API trong model; persistence runtime qua {@code CallBoardDAO}
 * (thường {@code InMemoryCallBoardDAO}). Staff ghi khi gọi / thủ tục / control ca;
 * Public Call đọc để dựng {@link PublicCallSnapshotDTO}.
 *
 * Vòng đời một field (ví dụ callingSbd):
 * - Staff gọi số → {@code StaffCallService.syncBoard} → {@code CallBoardRules.syncBoard} set calling
 * - {@code InMemoryCallBoardDAO.saveState} copy vào map JVM
 * - TV poll {@code /api/public-call/state} → {@code getState} → snapshot {@code calling}
 * - Vào bàn thủ tục → {@code occupyDesk} (giữ calling, set deskBusy)
 * - Xong thủ tục → {@code releaseDeskAndCall} (clear desk, calling mới)
 *
 * Ai tạo / cập nhật:
 * {@code CallBoardRules} (new/update); copy qua {@code InMemoryCallBoardDAO};
 * mutate từ {@code StaffCallServiceImpl} (get/sync/occupy/release/pause).
 *
 * Ai tiêu thụ:
 * {@code CandidateCallServlet}, {@code ProcedureServlet}, {@code PublicCallServlet} /
 * {@code PublicCallStateServlet}; {@code CandidateQueueServiceImpl#resolveSyncedCallingSbd}.
 *
 * Trang / JSP:
 * Không bind type này trực tiếp; field lộ qua session {@code callingSbd} và snapshot public TV.
 * - {@code callingSbd} — SBD đang được gọi (TV “Đang gọi”)
 * - {@code nextSbd} — SBD chuẩn bị gọi (TV “Tiếp theo”)
 * - {@code deskBusy}/{@code deskSbd} — bàn thủ tục đang bận (syncBoard không ghi đè calling)
 * - {@code queueOrderSbds} — thứ tự hàng đợi đồng bộ (cache; danh sách chi tiết lấy từ DB)
 * - {@code shiftEnded}/{@code examPaused} — trạng thái ca trên board (khác Status kỳ thi DB)
 * - {@code updatedAtMs} — client poll so sánh để biết có cần re-render
 */
public class CallBoardState {

    private int examId;
    private String callingSbd;
    private String nextSbd;
    private boolean shiftEnded;
    private boolean examPaused;
    private long updatedAtMs;
    private List<String> queueOrderSbds = new ArrayList<>();
    private boolean deskBusy;
    private String deskSbd;

    /** Kỳ thi mà board này đang mô tả. */
    public int getExamId() {
        return examId;
    }

    /** Gán kỳ thi của board. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** SBD đang chiếu / đang gọi. */
    public String getCallingSbd() {
        return callingSbd;
    }

    /** Gán SBD đang gọi. */
    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    /** SBD kế tiếp trên bảng gọi. */
    public String getNextSbd() {
        return nextSbd;
    }

    /** Gán SBD kế tiếp. */
    public void setNextSbd(String nextSbd) {
        this.nextSbd = nextSbd;
    }

    /** Ca đã end — dừng gọi mới. */
    public boolean isShiftEnded() {
        return shiftEnded;
    }

    /** Gán cờ kết thúc ca trên board. */
    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    /** Ca / kỳ đang pause trên board công khai. */
    public boolean isExamPaused() {
        return examPaused;
    }

    /** Gán cờ pause trên board. */
    public void setExamPaused(boolean examPaused) {
        this.examPaused = examPaused;
    }

    /** Thời điểm cập nhật gần nhất (epoch ms) — phục vụ poll TV. */
    public long getUpdatedAtMs() {
        return updatedAtMs;
    }

    /** Gán timestamp cập nhật board. */
    public void setUpdatedAtMs(long updatedAtMs) {
        this.updatedAtMs = updatedAtMs;
    }

    /** Thứ tự SBD đồng bộ hàng đợi (copy độc lập khi set). */
    public List<String> getQueueOrderSbds() {
        return queueOrderSbds;
    }

    /** Gán thứ tự hàng đợi (null → list rỗng mới; có giá trị → copy ArrayList). */
    public void setQueueOrderSbds(List<String> queueOrderSbds) {
        this.queueOrderSbds = queueOrderSbds != null ? new ArrayList<>(queueOrderSbds) : new ArrayList<>();
    }

    /** true khi bàn thủ tục đang chiếm bởi một thí sinh. */
    public boolean isDeskBusy() {
        return deskBusy;
    }

    /** Gán cờ bàn thủ tục bận. */
    public void setDeskBusy(boolean deskBusy) {
        this.deskBusy = deskBusy;
    }

    /** SBD đang chiếm bàn thủ tục. */
    public String getDeskSbd() {
        return deskSbd;
    }

    /** Gán SBD tại bàn thủ tục. */
    public void setDeskSbd(String deskSbd) {
        this.deskSbd = deskSbd;
    }
}
