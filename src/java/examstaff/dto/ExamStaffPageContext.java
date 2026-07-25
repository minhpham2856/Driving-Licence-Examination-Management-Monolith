package examstaff.dto;

import java.util.List;

/**
 * Ngữ cảnh trang ExamStaff đã chuẩn bị xong: kỳ thi hiện tại, danh sách kỳ, thí sinh và view picker.
 *
 * Vai trò trong luồng examstaff:
 * Đầu ra của bước prepareExamStaffPage: sau khi resolve examId (từ
 * ExamStaffPageCommand) và tùy chọn load candidates, servlet lấy context này để bind
 * attribute request / tiếp tục nghiệp vụ (gọi, thủ tục, phân bổ, báo cáo…).
 *
 * Ai tạo:
 * ExamStaffPageServiceImpl (new ExamStaffPageContext);
 * trả về qua ExamStaffPageSupport#prepareExamStaffPage.
 *
 * Ai tiêu thụ:
 * DashboardServlet, CandidateCallServlet, ProcedureServlet,
 * AllocationServlet, ReportServlet, ExaminerAllocationServlet
 * (chủ yếu lấy examId + candidates).
 *
 * Trang / JSP:
 * Gián tiếp qua binder: attributes allExams, currentExam, candidateQueue, …
 * trên dashboard.jsp, candidatecall.jsp, allocation / report / examiner-allocation.
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
