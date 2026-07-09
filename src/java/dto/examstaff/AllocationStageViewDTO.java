package dto.examstaff;

import dto.exam.ExamRegistrationDTO;
import util.examstaff.AllocationStageHelper;

import java.util.List;
import java.util.Set;

public class AllocationStageViewDTO {

    private Set<Integer> practicalStageIds;
    private AllocationStageHelper.StageCounts stageCounts;
    private List<ExamRegistrationDTO> stageList;
    private AllocationStageHelper.PageSlice<ExamRegistrationDTO> pageSlice;
    private List<AllocationOverviewHitDTO> overviewSearchHits;

    public Set<Integer> getPracticalStageIds() {
        return practicalStageIds;
    }

    public void setPracticalStageIds(Set<Integer> practicalStageIds) {
        this.practicalStageIds = practicalStageIds;
    }

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
