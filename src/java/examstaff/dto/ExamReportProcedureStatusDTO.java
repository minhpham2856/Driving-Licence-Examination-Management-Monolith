package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Thống kê trạng thái thủ tục cho báo cáo kỳ thi.
 *
 * <h2>Vai trò</h2>
 * Đếm thiếu ảnh / đã xong / đang chờ thủ tục và liệt kê thí sinh tương ứng trên {@code report.jsp}.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ExamReportProcedureStatusServiceImpl} → {@code ReportServlet}.
 */
public class ExamReportProcedureStatusDTO {

    private int missingPhotoCount;
    private int procedureCompleteCount;
    private int procedurePendingCount;
    private List<String> missingPhotoSbds;
    private List<ExamRegistrationDTO> missingPhotoCandidates;
    private List<ExamRegistrationDTO> procedurePendingCandidates;

    /** Số thí sinh thiếu ảnh thủ tục hợp lệ. */
    public int getMissingPhotoCount() {
        return missingPhotoCount;
    }

    /** Gán số thiếu ảnh. */
    public void setMissingPhotoCount(int missingPhotoCount) {
        this.missingPhotoCount = missingPhotoCount;
    }

    /** Số thí sinh đã hoàn tất thủ tục. */
    public int getProcedureCompleteCount() {
        return procedureCompleteCount;
    }

    /** Gán số đã xong thủ tục. */
    public void setProcedureCompleteCount(int procedureCompleteCount) {
        this.procedureCompleteCount = procedureCompleteCount;
    }

    /** Số thí sinh còn pending thủ tục. */
    public int getProcedurePendingCount() {
        return procedurePendingCount;
    }

    /** Gán số pending thủ tục. */
    public void setProcedurePendingCount(int procedurePendingCount) {
        this.procedurePendingCount = procedurePendingCount;
    }

    /** Danh sách SBD thiếu ảnh (tiện in / highlight). */
    public List<String> getMissingPhotoSbds() {
        return missingPhotoSbds;
    }

    /** Gán list SBD thiếu ảnh. */
    public void setMissingPhotoSbds(List<String> missingPhotoSbds) {
        this.missingPhotoSbds = missingPhotoSbds;
    }

    /** Hồ sơ đầy đủ các thí sinh thiếu ảnh. */
    public List<ExamRegistrationDTO> getMissingPhotoCandidates() {
        return missingPhotoCandidates;
    }

    /** Gán list thí sinh thiếu ảnh. */
    public void setMissingPhotoCandidates(List<ExamRegistrationDTO> missingPhotoCandidates) {
        this.missingPhotoCandidates = missingPhotoCandidates;
    }

    /** Hồ sơ thí sinh còn chờ hoàn tất thủ tục. */
    public List<ExamRegistrationDTO> getProcedurePendingCandidates() {
        return procedurePendingCandidates;
    }

    /** Gán list thí sinh pending thủ tục. */
    public void setProcedurePendingCandidates(List<ExamRegistrationDTO> procedurePendingCandidates) {
        this.procedurePendingCandidates = procedurePendingCandidates;
    }
}
