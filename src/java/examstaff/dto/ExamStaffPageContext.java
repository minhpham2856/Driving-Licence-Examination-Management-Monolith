package examstaff.dto;

import java.util.List;

/**
 * Ngữ cảnh trang ExamStaff đã chuẩn bị xong: kỳ thi hiện tại, danh sách kỳ, thí sinh và view picker.
 *
 * <h2>Vai trò trong luồng examstaff</h2>
 * Đầu ra của bước {@code prepareExamStaffPage}: sau khi resolve examId (từ
 * {@link ExamStaffPageCommand}) và tùy chọn load candidates, servlet lấy context này để bind
 * attribute request / tiếp tục nghiệp vụ (gọi, thủ tục, phân bổ, báo cáo…).
 *
 * <h2>Ai tạo</h2>
 * {@code ExamStaffPageServiceImpl} ({@code new ExamStaffPageContext});
 * trả về qua {@code ExamStaffPageSupport#prepareExamStaffPage}.
 *
 * <h2>Ai tiêu thụ</h2>
 * {@code DashboardServlet}, {@code CandidateCallServlet}, {@code ProcedureServlet},
 * {@code AllocationServlet}, {@code ReportServlet}, {@code ExaminerAllocationServlet}
 * (chủ yếu lấy {@code examId} + {@code candidates}).
 *
 * <h2>Trang / JSP</h2>
 * Gián tiếp qua binder: attributes {@code allExams}, {@code currentExam}, {@code candidateQueue}, …
 * trên {@code dashboard.jsp}, {@code candidatecall.jsp}, allocation / report / examiner-allocation.
 */
public class ExamStaffPageContext {

    private int examId;
    private List<ExamSummaryDTO> allExams;
    private List<ExamRegistrationDTO> candidates;
    private ExamStaffPickerViewDTO pickerView;

    /** Context rỗng — service set từng phần sau. */
    public ExamStaffPageContext() {
    }

    /**
     * Tạo context với kỳ thi, danh sách kỳ và thí sinh (null list → list rỗng bất biến).
     *
     * @param examId     kỳ thi hiện tại đã resolve
     * @param allExams   danh sách kỳ cho picker / điều hướng
     * @param candidates hàng thí sinh đã load (có thể rỗng nếu không load)
     */
    public ExamStaffPageContext(int examId, List<ExamSummaryDTO> allExams,
            List<ExamRegistrationDTO> candidates) {
        this.examId = examId;
        this.allExams = allExams != null ? allExams : List.of();
        this.candidates = candidates != null ? candidates : List.of();
    }

    /** Mã kỳ thi đang làm việc trên trang staff. */
    public int getExamId() {
        return examId;
    }

    /** Gán mã kỳ thi hiện tại. */
    public void setExamId(int examId) {
        this.examId = examId;
    }

    /** Toàn bộ kỳ thi staff có thể chọn (nguồn picker / sidebar). */
    public List<ExamSummaryDTO> getAllExams() {
        return allExams;
    }

    /** Gán danh sách kỳ thi cho ngữ cảnh trang. */
    public void setAllExams(List<ExamSummaryDTO> allExams) {
        this.allExams = allExams;
    }

    /** Danh sách thí sinh / đăng ký đã load cho kỳ hiện tại (có thể rỗng). */
    public List<ExamRegistrationDTO> getCandidates() {
        return candidates;
    }

    /** Gán danh sách thí sinh đã chuẩn bị cho trang. */
    public void setCandidates(List<ExamRegistrationDTO> candidates) {
        this.candidates = candidates;
    }

    /** View-model bộ chọn kỳ thi (options, current, committed id). */
    public ExamStaffPickerViewDTO getPickerView() {
        return pickerView;
    }

    /** Gán view picker kèm ngữ cảnh trang. */
    public void setPickerView(ExamStaffPickerViewDTO pickerView) {
        this.pickerView = pickerView;
    }
}
