package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Thống kê trạng thái thủ tục cho báo cáo kỳ thi.
 * Đếm thiếu ảnh / chờ thủ tục và danh sách thí sinh tương ứng.
 */
public class ExamReportProcedureStatusDTO {

    private int missingPhotoCount;
    private int procedureCompleteCount;
    private int procedurePendingCount;
    private List<String> missingPhotoSbds;
    private List<ExamRegistrationDTO> missingPhotoCandidates;
    private List<ExamRegistrationDTO> procedurePendingCandidates;

    public int getMissingPhotoCount() {
        return missingPhotoCount;
    }

    public void setMissingPhotoCount(int missingPhotoCount) {
        this.missingPhotoCount = missingPhotoCount;
    }

    public int getProcedureCompleteCount() {
        return procedureCompleteCount;
    }

    public void setProcedureCompleteCount(int procedureCompleteCount) {
        this.procedureCompleteCount = procedureCompleteCount;
    }

    public int getProcedurePendingCount() {
        return procedurePendingCount;
    }

    public void setProcedurePendingCount(int procedurePendingCount) {
        this.procedurePendingCount = procedurePendingCount;
    }

    public List<String> getMissingPhotoSbds() {
        return missingPhotoSbds;
    }

    public void setMissingPhotoSbds(List<String> missingPhotoSbds) {
        this.missingPhotoSbds = missingPhotoSbds;
    }

    public List<ExamRegistrationDTO> getMissingPhotoCandidates() {
        return missingPhotoCandidates;
    }

    public void setMissingPhotoCandidates(List<ExamRegistrationDTO> missingPhotoCandidates) {
        this.missingPhotoCandidates = missingPhotoCandidates;
    }

    public List<ExamRegistrationDTO> getProcedurePendingCandidates() {
        return procedurePendingCandidates;
    }

    public void setProcedurePendingCandidates(List<ExamRegistrationDTO> procedurePendingCandidates) {
        this.procedurePendingCandidates = procedurePendingCandidates;
    }
}
