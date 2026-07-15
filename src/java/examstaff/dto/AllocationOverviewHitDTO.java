package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

/**
 * Kết quả tìm kiếm trên trang tổng quan phân bổ.
 * Gắn thí sinh với stage (key/label/path) để điều hướng UI.
 */
public class AllocationOverviewHitDTO {

    private ExamRegistrationDTO candidate;
    private String stageKey;
    private String stageLabel;
    private String stagePath;

    public ExamRegistrationDTO getCandidate() {
        return candidate;
    }

    public void setCandidate(ExamRegistrationDTO candidate) {
        this.candidate = candidate;
    }

    public String getStageKey() {
        return stageKey;
    }

    public void setStageKey(String stageKey) {
        this.stageKey = stageKey;
    }

    public String getStageLabel() {
        return stageLabel;
    }

    public void setStageLabel(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    public String getStagePath() {
        return stagePath;
    }

    public void setStagePath(String stagePath) {
        this.stagePath = stagePath;
    }
}
