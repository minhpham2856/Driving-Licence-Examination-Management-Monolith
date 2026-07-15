package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.CallBoardState;

import java.util.ArrayList;
import java.util.List;

/**
 * Input command cho orchestrator trang gọi thí sinh (Candidate Call) — Presentation → BLL,
 * không phụ thuộc Servlet API trong chữ ký service.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Mang action gọi (gọi tiếp, vắng, đình chỉ, undo…), SBD mục tiêu, cờ ca (pause/end),
 * khóa mutation khi kỳ đã hoàn tất/hủy, snapshot board và cache hàng chờ.
 * {@code CandidateCallPageServiceImpl} xử lý rồi trả {@link CandidateCallPageViewDTO}.
 *
 * <h2>Ai tạo</h2>
 * {@code CandidateCallServlet#buildCommand} (đọc request + session + board).
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code StaffCallServiceImpl#preparePage} → {@code CandidateCallPageServiceImpl}.
 *
 * <h2>Trang / JSP</h2>
 * Không bind object command lên JSP; servlet forward kết quả sang
 * {@code candidatecall.jsp} hoặc {@code candidate-suspended.jsp}.
 */
public class CandidateCallPageCommand {

    private String action;
    private String sbd;
    private String view;
    private String returnView;
    private int examId;
    private int boardExamId;
    private int calledByStaffId;
    private String webRoot;
    private boolean shiftEnded;
    private boolean shiftPaused;
    /** Kỳ đã Hoàn tất/Hủy — chặn đình chỉ, hoàn tác, gọi tiếp... */
    private boolean examMutationsLocked;
    private String callingSbd;
    private Integer lastLoadedExamId;
    private List<String> callQueueOrder;
    private Integer callQueueOrderExamId;
    private List<ExamRegistrationDTO> permanentAbsents = new ArrayList<>();
    private List<ExamRegistrationDTO> cachedQueue = new ArrayList<>();
    private CallBoardState board;

    /** Mã hành động gọi từ request (call / absent / undo / suspend / …). */
    public String getAction() {
        return action;
    }

    /** Gán mã hành động gọi thí sinh. */
    public void setAction(String action) {
        this.action = action;
    }

    /** Số báo danh mục tiêu của thao tác (chuỗi SBD). */
    public String getSbd() {
        return sbd;
    }

    /** Gán SBD thí sinh thao tác. */
    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    /** View đang mở (ví dụ call / suspended) để quyết định forward. */
    public String getView() {
        return view;
    }

    /** Gán tên view hiện tại. */
    public void setView(String view) {
        this.view = view;
    }

    /** View cần quay lại sau redirect / PRG. */
    public String getReturnView() {
        return returnView;
    }

    /** Gán view quay lại sau xử lý. */
    public void setReturnView(String returnView) {
        this.returnView = returnView;
    }

    /** Kỳ thi staff đang thao tác trên bàn gọi. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi bàn gọi. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** ExamId gắn với {@link CallBoardState} đang đọc (có thể khác session nếu lệch sync). */
    public int getBoardExamId() {
        return boardExamId;
    }

    /** Gán examId của bảng gọi runtime. */
    public void setBoardExamId(int boardExamId) {
        this.boardExamId = boardExamId;
    }

    /** UserId staff thực hiện thao tác gọi (ghi CandidateCall / audit). */
    public int getCalledByStaffId() {
        return calledByStaffId;
    }

    /** Gán id staff đang gọi. */
    public void setCalledByStaffId(int calledByStaffId) {
        this.calledByStaffId = calledByStaffId;
    }

    /** Context path ứng dụng để build đường redirect. */
    public String getWebRoot() {
        return webRoot;
    }

    /** Gán web root. */
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    /** Ca đã kết thúc — UI/BLL chặn thao tác gọi tiếp. */
    public boolean isShiftEnded() {
        return shiftEnded;
    }

    /** Gán cờ ca đã kết thúc (từ session / board). */
    public void setShiftEnded(boolean shiftEnded) {
        this.shiftEnded = shiftEnded;
    }

    /** Ca đang tạm dừng (pause) — bảng gọi và desk phản ánh trạng thái. */
    public boolean isShiftPaused() {
        return shiftPaused;
    }

    /** Gán cờ ca đang pause. */
    public void setShiftPaused(boolean shiftPaused) {
        this.shiftPaused = shiftPaused;
    }

    /**
     * true khi kỳ thi đã khóa mutation (Hoàn tất / Hủy): chặn đình chỉ, undo, gọi tiếp, …
     */
    public boolean isExamMutationsLocked() {
        return examMutationsLocked;
    }

    /** Gán cờ khóa mọi thay đổi trạng thái gọi khi kỳ đã đóng. */
    public void setExamMutationsLocked(boolean examMutationsLocked) {
        this.examMutationsLocked = examMutationsLocked;
    }

    /** SBD đang được gọi trên session / desk trước khi xử lý action. */
    public String getCallingSbd() {
        return callingSbd;
    }

    /** Gán SBD đang gọi hiện tại. */
    public void setCallingSbd(String callingSbd) {
        this.callingSbd = callingSbd;
    }

    /** ExamId của lần load queue gần nhất (đối chiếu cache). */
    public Integer getLastLoadedExamId() {
        return lastLoadedExamId;
    }

    /** Gán examId lần load queue gần nhất. */
    public void setLastLoadedExamId(Integer lastLoadedExamId) {
        this.lastLoadedExamId = lastLoadedExamId;
    }

    /** Thứ tự SBD hàng gọi đang lưu session. */
    public List<String> getCallQueueOrder() {
        return callQueueOrder;
    }

    /** Gán thứ tự hàng gọi. */
    public void setCallQueueOrder(List<String> callQueueOrder) {
        this.callQueueOrder = callQueueOrder;
    }

    /** ExamId gắn với {@link #getCallQueueOrder()}. */
    public Integer getCallQueueOrderExamId() {
        return callQueueOrderExamId;
    }

    /** Gán examId của thứ tự hàng gọi. */
    public void setCallQueueOrderExamId(Integer callQueueOrderExamId) {
        this.callQueueOrderExamId = callQueueOrderExamId;
    }

    /** Danh sách thí sinh vắng vĩnh viễn trong ca (để UI / quy tắc hàng chờ). */
    public List<ExamRegistrationDTO> getPermanentAbsents() {
        return permanentAbsents;
    }

    /** Gán danh sách vắng vĩnh viễn (null → list rỗng). */
    public void setPermanentAbsents(List<ExamRegistrationDTO> permanentAbsents) {
        this.permanentAbsents = permanentAbsents != null ? permanentAbsents : new ArrayList<>();
    }

    /** Cache hàng chờ đầy đủ từ session để BLL thao tác không query lại nếu đủ. */
    public List<ExamRegistrationDTO> getCachedQueue() {
        return cachedQueue;
    }

    /** Gán cache hàng chờ (null → list rỗng). */
    public void setCachedQueue(List<ExamRegistrationDTO> cachedQueue) {
        this.cachedQueue = cachedQueue != null ? cachedQueue : new ArrayList<>();
    }

    /** Snapshot trạng thái bảng gọi runtime đồng bộ với ServletContext. */
    public CallBoardState getBoard() {
        return board;
    }

    /** Gán trạng thái bảng gọi hiện tại. */
    public void setBoard(CallBoardState board) {
        this.board = board;
    }
}
