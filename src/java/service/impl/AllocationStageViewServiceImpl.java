package service.impl;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationOverviewHitDTO;
import dto.examstaff.AllocationStageViewDTO;
import service.AllocationStageViewService;
import util.ExamRegistrationSort;
import util.examstaff.AllocationPassRules;
import util.examstaff.AllocationStageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllocationStageViewServiceImpl implements AllocationStageViewService {

    @Override
    public AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec) {
        AllocationStageViewDTO view = new AllocationStageViewDTO();
        if (candidates == null) {
            view.setPracticalStageIds(Set.of());
            view.setNoRoadTestIds(Set.of());
            view.setStageCounts(new AllocationStageUtil.StageCounts());
            view.setStageList(List.of());
            view.setPageSlice(new AllocationStageUtil.PageSlice<>(List.of(), page, pageSize, 0));
            view.setOverviewSearchHits(List.of());
            return view;
        }

        Set<Integer> practicalStageIds = new HashSet<>();
        Set<Integer> noRoadTestIds = new HashSet<>();
        for (ExamRegistrationDTO candidate : candidates) {
            AllocationPassRules.applyToCandidate(candidate);
            if (AllocationPassRules.isPracticalStageEligible(candidate)) {
                practicalStageIds.add(candidate.getId());
            }
            String license = AllocationPassRules.normalizeLicense(
                    candidate.getLicenseCode(), candidate.getClazz());
            if (!AllocationPassRules.requiresRoadTest(license) || candidate.skipsRoad()) {
                noRoadTestIds.add(candidate.getId());
            }
        }

        List<ExamRegistrationDTO> stageFiltered = new ArrayList<>();
        if (!AllocationStageUtil.STAGE_OVERVIEW.equals(stage)) {
            String filter = AllocationStageUtil.STAGE_RESULTS.equals(stage) ? resultFilter : null;
            stageFiltered = AllocationStageUtil.filterForStage(candidates, stage, practicalStageIds, filter);
            stageFiltered = AllocationStageUtil.filterSearch(stageFiltered, searchQuery);
        }
        ExamRegistrationSort.sort(stageFiltered, sortSpec);
        AllocationStageUtil.PageSlice<ExamRegistrationDTO> slice
                = AllocationStageUtil.paginate(stageFiltered, page, pageSize);

        view.setPracticalStageIds(practicalStageIds);
        view.setNoRoadTestIds(noRoadTestIds);
        view.setStageCounts(AllocationStageUtil.computeCounts(candidates, practicalStageIds));
        view.setStageList(slice.getItems());
        view.setPageSlice(slice);
        view.setOverviewSearchHits(buildOverviewHits(candidates, practicalStageIds, stage, searchQuery));
        return view;
    }

    private static List<AllocationOverviewHitDTO> buildOverviewHits(
            List<ExamRegistrationDTO> candidates, Set<Integer> practicalStageIds,
            String stage, String searchQuery) {
        if (!AllocationStageUtil.STAGE_OVERVIEW.equals(stage)
                || searchQuery == null || searchQuery.isBlank()) {
            return List.of();
        }
        List<ExamRegistrationDTO> matched = AllocationStageUtil.filterSearch(candidates, searchQuery);
        List<AllocationOverviewHitDTO> hits = new ArrayList<>(matched.size());
        for (ExamRegistrationDTO candidate : matched) {
            String stageKey = AllocationStageUtil.resolveCurrentStageKey(candidate, practicalStageIds);
            AllocationOverviewHitDTO hit = new AllocationOverviewHitDTO();
            hit.setCandidate(candidate);
            hit.setStageKey(stageKey);
            hit.setStageLabel(AllocationStageUtil.stageLabel(stageKey));
            hit.setStagePath(AllocationStageUtil.stageServletPath(stageKey));
            hits.add(hit);
        }
        return hits;
    }
}
