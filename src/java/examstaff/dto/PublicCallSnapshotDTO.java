package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Snapshot chỉ-đọc cho màn Public Call (TV / bảng gọi công khai): JSP lần đầu và JSON poll.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Tổng hợp từ {@link CallBoardState} + hàng chờ đã resolve: thí sinh đang gọi, kế tiếp,
 * danh sách chờ, cờ desk bận / ca pause / ca kết thúc. Không cho phép mutation từ phía TV.
 *
 * <h2>Ai tạo</h2>
 * {@code StaffCallServiceImpl#loadPublicSnapshot} (hỗ trợ {@code PublicCallSnapshotSupport}).
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code PublicCallServlet} (render JSP), {@code PublicCallStateServlet} (JSON);
 * binder {@code PublicCallSnapshotSupport#bindRequest} / {@code toStateJson}.
 *
 * <h2>Trang / endpoint</h2>
 * {@code web/views/public/public-call.jsp}; API poll {@code /api/public-call/state}.
 */
public class PublicCallSnapshotDTO {

    private int examId;
    private ExamSummaryDTO currentExam;
    private ExamRegistrationDTO callingCandidate;
    private ExamRegistrationDTO nextCandidate;
    private List<ExamRegistrationDTO> waitingQueue;
    private boolean callingActive;
    private boolean shiftEnded;
    private boolean examPaused;
    private long updatedAtMs;
    private boolean deskBusy;
    private String deskSbd;

    /** Mã kỳ thi đang chiếu trên bảng gọi công khai. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi của snapshot. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Tóm tắt kỳ thi (tên, giờ ca, hạng…) hiển thị header TV. */
    public ExamSummaryDTO getCurrentExam() {
        return currentExam;
    }

    /** Gán thông tin kỳ thi đang chiếu. */
    public void setCurrentExam(ExamSummaryDTO currentExam) {
        this.currentExam = currentExam;
    }

    /** Thí sinh đang được gọi (null nếu chưa có / đã clear). */
    public ExamRegistrationDTO getCallingCandidate() {
        return callingCandidate;
    }

    /** Gán thí sinh slot đang gọi. */
    public void setCallingCandidate(ExamRegistrationDTO callingCandidate) {
        this.callingCandidate = callingCandidate;
    }

    /** Thí sinh kế tiếp trên bảng (next). */
    public ExamRegistrationDTO getNextCandidate() {
        return nextCandidate;
    }

    /** Gán thí sinh kế tiếp. */
    public void setNextCandidate(ExamRegistrationDTO nextCandidate) {
        this.nextCandidate = nextCandidate;
    }

    /** Hàng chờ công khai (các SBD còn lại sau calling/next theo quy tắc hiển thị). */
    public List<ExamRegistrationDTO> getWaitingQueue() {
        return waitingQueue;
    }

    /** Gán danh sách chờ trên TV. */
    public void setWaitingQueue(List<ExamRegistrationDTO> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    /** true nếu đang có slot gọi active (có callingSbd hợp lệ). */
    public boolean isCallingActive() {
        return callingActive;
    }

    /** Gán cờ đang trong phiên gọi. */
    public void setCallingActive(boolean callingActive) {
        this.callingActive = callingActive;
    }

    /** Ca đã kết thúc — TV hiển thị trạng thái end. */
    public boolean isShiftEnded() {
        return shiftEnded;
    }

    /** Gán cờ ca kết thúc trên snapshot. */
    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    /** Kỳ / ca đang tạm dừng — TV hiển thị pause. */
    public boolean isExamPaused() {
        return examPaused;
    }

    /** Gán cờ tạm dừng trên snapshot. */
    public void setExamPaused(boolean examPaused) {
        this.examPaused = examPaused;
    }

    /** Timestamp ms lần cập nhật board (client dùng để biết data mới). */
    public long getUpdatedAtMs() {
        return updatedAtMs;
    }

    /** Gán thời điểm cập nhật snapshot. */
    public void setUpdatedAtMs(long updatedAtMs) {
        this.updatedAtMs = updatedAtMs;
    }

    /** Bàn thủ tục đang bận (thí sinh đang làm thủ tục tại desk). */
    public boolean isDeskBusy() {
        return deskBusy;
    }

    /** Gán cờ desk bận cho TV. */
    public void setDeskBusy(boolean deskBusy) {
        this.deskBusy = deskBusy;
    }

    /** SBD đang chiếm bàn thủ tục (khi deskBusy). */
    public String getDeskSbd() {
        return deskSbd;
    }

    /** Gán SBD đang ở bàn thủ tục. */
    public void setDeskSbd(String deskSbd) {
        this.deskSbd = deskSbd;
    }
}
