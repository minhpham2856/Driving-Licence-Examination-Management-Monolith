package examstaff.dto;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.service.impl.support.allocation.AllocationStageHelper;

import java.util.List;

/**
 * View-model trang phân bổ thí sinh theo stage (BLL → JSP allocation).
 *
 * <h2>Vai trò</h2>
 * Mang số đếm từng stage, danh sách thí sinh stage hiện tại, phân trang và hit tìm kiếm tổng quan.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code AllocationStageViewServiceImpl} → {@code AllocationServlet} → các JSP allocation.
 */
public class AllocationStageViewDTO {

    private AllocationStageHelper.StageCounts stageCounts;
    private List<ExamRegistrationDTO> stageList;
    private AllocationStageHelper.PageSlice<ExamRegistrationDTO> pageSlice;
    private List<AllocationOverviewHitDTO> overviewSearchHits;

    /** Bộ đếm thí sinh theo từng stage phân bổ (sidebar / badge). */
    public AllocationStageHelper.StageCounts getStageCounts() {
        return stageCounts;
    }

    /** Gán bộ đếm stage. */
    public void setStageCounts(AllocationStageHelper.StageCounts stageCounts) {
        this.stageCounts = stageCounts;
    }

    /** Danh sách thí sinh thuộc stage đang mở. */
    public List<ExamRegistrationDTO> getStageList() {
        return stageList;
    }

    /** Gán list thí sinh stage hiện tại. */
    public void setStageList(List<ExamRegistrationDTO> stageList) {
        this.stageList = stageList;
    }

    /** Phân trang list stage (page/size/total). */
    public AllocationStageHelper.PageSlice<ExamRegistrationDTO> getPageSlice() {
        return pageSlice;
    }

    /** Gán slice phân trang stage. */
    public void setPageSlice(AllocationStageHelper.PageSlice<ExamRegistrationDTO> pageSlice) {
        this.pageSlice = pageSlice;
    }

    /** Kết quả tìm kiếm trên màn overview (map thí sinh → stage). */
    public List<AllocationOverviewHitDTO> getOverviewSearchHits() {
        return overviewSearchHits;
    }

    /** Gán hit tìm kiếm tổng quan phân bổ. */
    public void setOverviewSearchHits(List<AllocationOverviewHitDTO> overviewSearchHits) {
        this.overviewSearchHits = overviewSearchHits;
    }
}
