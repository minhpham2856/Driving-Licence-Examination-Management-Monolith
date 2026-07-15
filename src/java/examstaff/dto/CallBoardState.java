package examstaff.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Trạng thái runtime bảng gọi thí sinh (in-memory) — nguồn sự thật chia sẻ giữa staff desk và Public Call.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Lưu calling / next / thứ tự queue / desk busy / pause / end theo từng {@code examId}.
 * Không phụ thuộc Servlet API trong model; persistence runtime qua {@code CallBoardDAO}
 * (thường {@code ServletContextCallBoardDAO}). Staff ghi khi gọi / thủ tục / control ca;
 * Public Call đọc để dựng {@link PublicCallSnapshotDTO}.
 *
 * <h2>Ai tạo / cập nhật</h2>
 * {@code CallBoardRules} (new/update); copy qua {@code ServletContextCallBoardDAO};
 * mutate từ {@code StaffCallServiceImpl} (get/sync/occupy/release/pause).
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code CandidateCallServlet}, {@code ProcedureServlet}, {@code PublicCallServlet} /
 * {@code PublicCallStateServlet}; {@code CandidateQueueServiceImpl#resolveSyncedCallingSbd}.
 *
 * <h2>Trang / JSP</h2>
 * Không bind type này trực tiếp; field lộ qua session {@code callingSbd} và snapshot public TV.
 *
 * <ul>
 *   <li>{@code callingSbd} — SBD đang được gọi</li>
 *   <li>{@code nextSbd} — SBD chuẩn bị gọi</li>
 *   <li>{@code deskBusy}/{@code deskSbd} — bàn thủ tục đang bận</li>
 *   <li>{@code queueOrderSbds} — thứ tự hàng đợi đồng bộ</li>
 *   <li>{@code shiftEnded}/{@code examPaused} — trạng thái ca</li>
 * </ul>
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
