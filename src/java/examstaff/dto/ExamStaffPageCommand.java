package examstaff.dto;

import java.util.List;

/**
 * Command đầu vào khi chuẩn bị / chuyển / resolve / refresh trang ExamStaff
 * (Presentation → BLL, không phụ thuộc Servlet API trực tiếp trong chữ ký service).
 *
 * Vai trò trong luồng examstaff:
 * Gom tham số từ URL, session và cache: examId đang chọn, kỳ trước, có load danh sách thí sinh không,
 * danh sách kỳ để picker, cache hàng chờ và thứ tự gọi. BLL dùng command này để resolve kỳ thi hiện tại
 * và (tuỳ cờ) nạp candidates trước khi trả {@link ExamStaffPageContext}.
 *
 * Ai tạo:
 * - {@code ExamStaffPageSupport} — {@code buildPagePrepareInput} / transition / selection.
 * - Servlet hỗ trợ: {@code ProcedureServlet}, {@code AllocationServlet}, {@code ExamSelectServlet}.
 * - Service refresh: {@code ExamStaffPageServiceImpl}, {@code CandidateCallPageServiceImpl}.
 *
 * Ai tiêu thụ:
 * {@code ExamStaffViewServiceImpl}, {@code ExamStaffPageServiceImpl}, {@code ExamStaffSelectionServiceImpl};
 * {@code CandidateQueueServiceImpl#refreshQueue}; facade {@code StaffCallService} / {@code ExamStaffViewService}.
 *
 * Trang / servlet:
 * Không bind object này lên JSP. Kết quả sau xử lý phục vụ
 * Dashboard, Candidate Call, Procedure, Allocation, Report, Audit, Examiner Allocation, Exam Select
 * (thường qua {@link ExamStaffPageContext} + attributes).
 */
public class ExamStaffPageCommand {

    private int urlExamId;
    private Integer previousExamId;
    private Integer selectedExamId;
    private Integer loadedExamId;
    private String examIdParam;
    private boolean loadCandidates;
    private String webRoot;
    private List<ExamSummaryDTO> allExams;
    private List<ExamRegistrationDTO> cachedQueue;
    private List<String> callQueueOrder;
    private Integer callQueueOrderExamId;
    private int examId;
    private int defaultExamId;

    /** ExamId lấy từ query URL (0 nếu không có / không parse được). */
    public int getUrlExamId() {
        return urlExamId;
    }

    /** Gán examId từ tham số URL. */
    public void setUrlExamId(int urlExamId) {
        this.urlExamId = urlExamId;
    }

    /** Kỳ thi đang giữ trong session trước khi chuyển / resolve lần này. */
    public Integer getPreviousExamId() {
        return previousExamId;
    }

    /** Gán kỳ thi session trước đó (để so sánh đổi kỳ). */
    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    /** Kỳ thi user chọn trên picker (POST select-exam / đổi kỳ). */
    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    /** Gán kỳ thi được chọn từ form picker. */
    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    /** ExamId của cache thí sinh đã load lần trước (để biết có tái dùng cache không). */
    public Integer getLoadedExamId() {
        return loadedExamId;
    }

    /** Gán examId gắn với cache candidates hiện có. */
    public void setLoadedExamId(Integer loadedExamId) {
        this.loadedExamId = loadedExamId;
    }

    /** Chuỗi tham số examId thô từ request (trước khi parse số). */
    public String getExamIdParam() {
        return examIdParam;
    }

    /** Gán chuỗi examId từ request. */
    public void setExamIdParam(String examIdParam) {
        this.examIdParam = examIdParam;
    }

    /** true nếu BLL cần nạp danh sách thí sinh (không chỉ resolve examId). */
    public boolean isLoadCandidates() {
        return loadCandidates;
    }

    /** Gán cờ có load candidates khi chuẩn bị trang. */
    public void setLoadCandidates(boolean loadCandidates) {
        this.loadCandidates = loadCandidates;
    }

    /** Đường dẫn web root (context path) phục vụ build URL redirect / asset. */
    public String getWebRoot() {
        return webRoot;
    }

    /** Gán web root của ứng dụng. */
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    /** Danh sách kỳ thi khả dụng cho picker / resolve mặc định. */
    public List<ExamSummaryDTO> getAllExams() {
        return allExams;
    }

    /** Gán danh sách kỳ thi đưa vào bước chuẩn bị trang. */
    public void setAllExams(List<ExamSummaryDTO> allExams) {
        this.allExams = allExams;
    }

    /** Cache hàng chờ thí sinh trong session (tránh query lại nếu cùng kỳ). */
    public List<ExamRegistrationDTO> getCachedQueue() {
        return cachedQueue;
    }

    /** Gán cache hàng chờ từ session. */
    public void setCachedQueue(List<ExamRegistrationDTO> cachedQueue) {
        this.cachedQueue = cachedQueue;
    }

    /** Thứ tự SBD trên bảng gọi (session) — giữ thứ tự gọi khi refresh. */
    public List<String> getCallQueueOrder() {
        return callQueueOrder;
    }

    /** Gán thứ tự hàng gọi theo SBD. */
    public void setCallQueueOrder(List<String> callQueueOrder) {
        this.callQueueOrder = callQueueOrder;
    }

    /** ExamId mà {@link #getCallQueueOrder()} đang gắn (tránh dùng nhầm thứ tự kỳ khác). */
    public Integer getCallQueueOrderExamId() {
        return callQueueOrderExamId;
    }

    /** Gán examId gắn với thứ tự hàng gọi. */
    public void setCallQueueOrderExamId(Integer callQueueOrderExamId) {
        this.callQueueOrderExamId = callQueueOrderExamId;
    }

    /** ExamId đã resolve sau bước chọn / URL / mặc định (đầu ra trung gian trên command). */
    public int getExamId() {
        return examId;
    }

    /** Gán examId đã resolve. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** ExamId mặc định khi không có URL/session (ví dụ kỳ đầu tiên trong danh sách). */
    public int getDefaultExamId() {
        return defaultExamId;
    }

    /** Gán examId mặc định fallback. */
    public void setDefaultExamId(int defaultExamId) {
        this.defaultExamId = defaultExamId;
    }
}
