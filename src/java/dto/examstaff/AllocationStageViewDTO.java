package dto.examstaff;

import dto.exam.ExamRegistrationDTO;
import util.examstaff.AllocationStageUtil;

import java.util.List;
import java.util.Set;

public class AllocationStageViewDTO {

    private Set<Integer> practicalStageIds;
    private Set<Integer> noRoadTestIds;
    private AllocationStageUtil.StageCounts stageCounts;
    private List<ExamRegistrationDTO> stageList;
    private AllocationStageUtil.PageSlice<ExamRegistrationDTO> pageSlice;
    private List<AllocationOverviewHitDTO> overviewSearchHits;

    public Set<Integer> getPracticalStageIds() {
        return practicalStageIds;
    }

    public void setPracticalStageIds(Set<Integer> practicalStageIds) {
        this.practicalStageIds = practicalStageIds;
    }

    public Set<Integer> getNoRoadTestIds() {
        return noRoadTestIds;
    }

    public void setNoRoadTestIds(Set<Integer> noRoadTestIds) {
        this.noRoadTestIds = noRoadTestIds;
    }

    public AllocationStageUtil.StageCounts getStageCounts() {
        return stageCounts;
    }

    public void setStageCounts(AllocationStageUtil.StageCounts stageCounts) {
        this.stageCounts = stageCounts;
    }

    public List<ExamRegistrationDTO> getStageList() {
        return stageList;
    }

    public void setStageList(List<ExamRegistrationDTO> stageList) {
        this.stageList = stageList;
    }

    public AllocationStageUtil.PageSlice<ExamRegistrationDTO> getPageSlice() {
        return pageSlice;
    }

    public void setPageSlice(AllocationStageUtil.PageSlice<ExamRegistrationDTO> pageSlice) {
        this.pageSlice = pageSlice;
    }

    public List<AllocationOverviewHitDTO> getOverviewSearchHits() {
        return overviewSearchHits;
    }

    public void setOverviewSearchHits(List<AllocationOverviewHitDTO> overviewSearchHits) {
        this.overviewSearchHits = overviewSearchHits;
    }
}
