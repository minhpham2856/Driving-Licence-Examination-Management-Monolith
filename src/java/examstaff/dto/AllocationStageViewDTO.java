package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.util.AllocationStageHelper;

import java.util.List;

/**
 * View-model trang phân bổ theo stage (BLL → JSP).
 * Gồm đếm stage, danh sách thí sinh, phân trang và hit tìm kiếm tổng quan.
 */
public class AllocationStageViewDTO {

    private AllocationStageHelper.StageCounts stageCounts;
    private List<ExamRegistrationDTO> stageList;
    private AllocationStageHelper.PageSlice<ExamRegistrationDTO> pageSlice;
    private List<AllocationOverviewHitDTO> overviewSearchHits;

    public AllocationStageHelper.StageCounts getStageCounts() {
        return stageCounts;
    }

    public void setStageCounts(AllocationStageHelper.StageCounts stageCounts) {
        this.stageCounts = stageCounts;
    }

    public List<ExamRegistrationDTO> getStageList() {
        return stageList;
    }

    public void setStageList(List<ExamRegistrationDTO> stageList) {
        this.stageList = stageList;
    }

    public AllocationStageHelper.PageSlice<ExamRegistrationDTO> getPageSlice() {
        return pageSlice;
    }

    public void setPageSlice(AllocationStageHelper.PageSlice<ExamRegistrationDTO> pageSlice) {
        this.pageSlice = pageSlice;
    }

    public List<AllocationOverviewHitDTO> getOverviewSearchHits() {
        return overviewSearchHits;
    }

    public void setOverviewSearchHits(List<AllocationOverviewHitDTO> overviewSearchHits) {
        this.overviewSearchHits = overviewSearchHits;
    }
}
