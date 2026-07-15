package examstaff.dto;

/**
 * Kết quả bước chuyển / chọn kỳ thi (select-exam / sync selection) trong luồng ExamStaff.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Báo cho Presentation biết kỳ mới là gì, có cần xóa cache thí sinh / state thủ tục trên session không,
 * và có persist lựa chọn vào session hay không. Thường được bọc trong {@link ServiceResult}{@code ExamSelectServlet}.
 *
 * <h2>Ai tạo</h2>
 * {@code ExamStaffSelectionServiceImpl} — {@code syncExamSelection}, {@code preparePageTransition},
 * {@code processSelection}.
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code ExamStaffPageSupport} (áp dụng cờ clear cache/procedure khi đổi kỳ);
 * {@code ExamSelectServlet} qua {@code ServiceResult<ExamTransitionResultDTO>}.
 *
 * <h2>Trang / JSP</h2>
 * Không bind DTO lên JSP; điều khiển session rồi PRG redirect tới {@code /examstaff/select-exam}
 * hoặc trang staff đích.
 */
public class ExamTransitionResultDTO {

    private boolean success = true;
    private int examId;
    private int newExamId;
    private Integer previousExamId;
    private String errorMessage;
    private boolean clearCandidateCache;
    private boolean clearProcedureState;
    private boolean persistSelection;

    /** true nếu chọn / chuyển kỳ thành công. */
    public boolean isSuccess() {
        return success;
    }

    /** Gán kết quả thành công / thất bại của bước chuyển kỳ. */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** ExamId hiệu lực sau resolve (thường trùng {@link #getNewExamId()} khi thành công). */
    public int getExamId() {
        return examId;
    }

    /** Gán examId hiệu lực. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Mã kỳ thi mới được chọn sau thao tác chuyển. */
    public int getNewExamId() {
        return newExamId;
    }

    /** Gán mã kỳ thi mới. */
    public void setNewExamId(int newExamId) {
        this.newExamId = newExamId;
    }

    /** Kỳ thi trước đó trên session (null nếu chưa có). */
    public Integer getPreviousExamId() {
        return previousExamId;
    }

    /** Gán kỳ thi trước khi chuyển. */
    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    /** Thông báo lỗi nghiệp vụ khi {@link #isSuccess()} = false (hiển thị flash). */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Gán thông báo lỗi chuyển kỳ. */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** Yêu cầu Presentation xóa cache danh sách thí sinh trên session. */
    public boolean isClearCandidateCache() {
        return clearCandidateCache;
    }

    /** Gán cờ xóa cache thí sinh sau đổi kỳ. */
    public void setClearCandidateCache(boolean clearCandidateCache) {
        this.clearCandidateCache = clearCandidateCache;
    }

    /** Yêu cầu xóa state wizard thủ tục (profile/payment vừa làm) khi đổi kỳ. */
    public boolean isClearProcedureState() {
        return clearProcedureState;
    }

    /** Gán cờ xóa state thủ tục trên session. */
    public void setClearProcedureState(boolean clearProcedureState) {
        this.clearProcedureState = clearProcedureState;
    }

    /** true nếu cần ghi examId đã chọn vào session để các trang sau dùng. */
    public boolean isPersistSelection() {
        return persistSelection;
    }

    /** Gán cờ persist lựa chọn kỳ thi vào session. */
    public void setPersistSelection(boolean persistSelection) {
        this.persistSelection = persistSelection;
    }
}
