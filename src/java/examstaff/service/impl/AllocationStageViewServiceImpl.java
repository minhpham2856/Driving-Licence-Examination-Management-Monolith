package examstaff.service.impl;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationOverviewHitDTO;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.service.AllocationStageViewService;
import examstaff.util.ExamRegistrationSort;
import examstaff.util.AllocationPassRules;
import examstaff.util.AllocationStageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Implementation: dựng view phân bổ theo giai đoạn (LT / TH / tổng quan). */
public class AllocationStageViewServiceImpl implements AllocationStageViewService {

    /** {@inheritDoc} */
    @Override
    public AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewDTO view = new AllocationStageViewDTO();
        if (candidates == null) {
            view.setStageCounts(new AllocationStageHelper.StageCounts());
            view.setStageList(List.of());
            view.setPageSlice(new AllocationStageHelper.PageSlice<>(List.of(), page, pageSize, 0));
            view.setOverviewSearchHits(List.of());
            return view;
        }

        Set<Integer> practicalStageIds = new HashSet<>();
        for (ExamRegistrationDTO candidate : candidates) {
            AllocationPassRules.applyToCandidate(candidate);
            if (AllocationPassRules.isPracticalStageEligible(candidate)) {
                practicalStageIds.add(candidate.getId());
            }
        }

        List<ExamRegistrationDTO> stageFiltered = new ArrayList<>();
        if (!AllocationStageHelper.STAGE_OVERVIEW.equals(stage)) {
            String filter = AllocationStageHelper.STAGE_RESULTS.equals(stage) ? resultFilter : null;
            stageFiltered = AllocationStageHelper.filterForStage(candidates, stage, practicalStageIds, filter);
            stageFiltered = AllocationStageHelper.filterSearch(stageFiltered, searchQuery);
            if (AllocationStageHelper.STAGE_THEORY.equals(stage)
                    || AllocationStageHelper.STAGE_PRACTICAL.equals(stage)) {
                stageFiltered = AllocationStageHelper.filterByAllocatedArea(
                        stageFiltered, areaFilterId,
                        AllocationStageHelper.STAGE_PRACTICAL.equals(stage));
            }
        }
        ExamRegistrationSort.sort(stageFiltered, sortSpec);
        AllocationStageHelper.PageSlice<ExamRegistrationDTO> slice
                = AllocationStageHelper.paginate(stageFiltered, page, pageSize);

        view.setStageCounts(AllocationStageHelper.computeCounts(candidates, practicalStageIds));
        view.setStageList(slice.getItems());
        view.setPageSlice(slice);
        view.setOverviewSearchHits(buildOverviewHits(candidates, practicalStageIds, stage, searchQuery));
        return view;
    }

    /** Kết quả tìm kiếm nhanh trên stage tổng quan. */
    private static List<AllocationOverviewHitDTO> buildOverviewHits(
            List<ExamRegistrationDTO> candidates, Set<Integer> practicalStageIds,
            String stage, String searchQuery) {
        if (!AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || searchQuery == null || searchQuery.isBlank()) {
            return List.of();
        }
        List<ExamRegistrationDTO> matched = AllocationStageHelper.filterSearch(candidates, searchQuery);
        List<AllocationOverviewHitDTO> hits = new ArrayList<>(matched.size());
        for (ExamRegistrationDTO candidate : matched) {
            String stageKey = AllocationStageHelper.resolveCurrentStageKey(candidate, practicalStageIds);
            AllocationOverviewHitDTO hit = new AllocationOverviewHitDTO();
            hit.setCandidate(candidate);
            hit.setStageKey(stageKey);
            hit.setStageLabel(AllocationStageHelper.stageLabel(stageKey));
            hit.setStagePath(AllocationStageHelper.stageServletPath(stageKey));
            hits.add(hit);
        }
        return hits;
    }
}
