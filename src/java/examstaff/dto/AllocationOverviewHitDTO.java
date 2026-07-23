package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;

/**
 * Một hit tìm kiếm trên trang tổng quan phân bổ.
 *
 * Vai trò:
 * Gắn thí sinh với stage (key/label/path) để UI điều hướng đúng màn stage sau khi search.
 *
 * Ai tạo / tiêu thụ:
 * {@code AllocationStageViewServiceImpl} → nằm trong {@link AllocationStageViewDTO#getOverviewSearchHits()}
 * → JSP allocation overview.
 */
public class AllocationOverviewHitDTO {

    private ExamRegistrationDTO candidate;
    private String stageKey;
    private String stageLabel;
    private String stagePath;

    /** Thí sinh khớp từ khóa tìm kiếm. */
    public ExamRegistrationDTO getCandidate() {
        return candidate;
    }

    /** Gán thí sinh của hit. */
    public void setCandidate(ExamRegistrationDTO candidate) {
        this.candidate = candidate;
    }

    /** Khóa stage nội bộ (ví dụ theory_pending). */
    public String getStageKey() {
        return stageKey;
    }

    /** Gán khóa stage. */
    public void setStageKey(String stageKey) {
        this.stageKey = stageKey;
    }

    /** Nhãn stage hiển thị tiếng Việt / UI. */
    public String getStageLabel() {
        return stageLabel;
    }

    /** Gán nhãn stage. */
    public void setStageLabel(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    /** Đường dẫn servlet/JSP tới màn stage tương ứng. */
    public String getStagePath() {
        return stagePath;
    }

    /** Gán path điều hướng stage. */
    public void setStagePath(String stagePath) {
        this.stagePath = stagePath;
    }
}
