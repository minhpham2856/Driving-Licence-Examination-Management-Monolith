package examstaff.service.impl.support.allocation;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.AllocationOverviewHitDTO;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.util.ExamRegistrationSort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dựng view phân bổ theo giai đoạn (LT / TH / tổng quan / results).
 *
 * Cách hoạt động:
 * Nhận list {@link ExamRegistrationDTO} đã load từ DB →
 * {@link AllocationStageHelper} lọc theo stage/result → search/sort/paging →
 * {@link AllocationStageViewDTO} (kèm {@link AllocationStageHelper.StageCounts} cho tab).
 * Không gọi JDBC; chỉ biến đổi in-memory cho JSP.
 */
public class AllocationStageViewServiceImpl {

    /**
     * Ghép danh sách thí sinh phân phòng theo stage, bộ lọc và sắp xếp.
     * @param candidates   danh sách nguồn
     * @param stage        giai đoạn (ví dụ lý thuyết / thực hành / overview)
     * @param resultFilter lọc kết quả đỗ/trượt (có thể rỗng; dùng khi stage = results)
     * @param searchQuery  từ khóa tìm kiếm (có thể rỗng)
     * @param page         trang hiện tại
     * @param pageSize     kích thước trang
     * @param sortSpec     quy tắc sắp xếp
     * @param areaFilterId lọc theo khu vực ({@code null} = không lọc)
     * @return DTO view phân phòng theo stage
     */
    public AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        AllocationStageViewDTO view = new AllocationStageViewDTO();
        // validate: null → view rỗng
        if (candidates == null) {
            view.setStageCounts(new AllocationStageHelper.StageCounts());
            view.setStageList(List.of());
            view.setPageSlice(new AllocationStageHelper.PageSlice<>(List.of(), page, pageSize, 0));
            view.setOverviewSearchHits(List.of());
            return view;
        }

        // load: áp cờ đạt + tập id đủ điều kiện stage TH
        Set<Integer> practicalStageIds = new HashSet<>();
        for (ExamRegistrationDTO candidate : candidates) {
            AllocationPassRules.applyToCandidate(candidate);
            if (AllocationPassRules.isPracticalStageEligible(candidate)) {
                practicalStageIds.add(candidate.getId());
            }
        }

        // mutate list: lọc stage / search / area (bỏ qua khi overview)
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
        // mutate: sort + phân trang
        ExamRegistrationSort.sort(stageFiltered, sortSpec);
        AllocationStageHelper.PageSlice<ExamRegistrationDTO> slice
                = AllocationStageHelper.paginate(stageFiltered, page, pageSize);

        // result: đếm + list trang + hit tìm overview
        view.setStageCounts(AllocationStageHelper.computeCounts(candidates, practicalStageIds));
        view.setStageList(slice.getItems());
        view.setPageSlice(slice);
        view.setOverviewSearchHits(buildOverviewHits(candidates, practicalStageIds, stage, searchQuery));
        return view;
    }

    /**
     * Kết quả tìm kiếm nhanh trên stage tổng quan (gắn nhãn stage hiện tại của từng thí sinh).
     * @param candidates        danh sách nguồn
     * @param practicalStageIds id thí sinh đang stage thực hành
     * @param stage             stage hiện tại (chỉ xử lý khi overview)
     * @param searchQuery       từ khóa (blank → rỗng)
     * @return danh sách hit overview
     */
    private static List<AllocationOverviewHitDTO> buildOverviewHits(
            List<ExamRegistrationDTO> candidates, Set<Integer> practicalStageIds,
            String stage, String searchQuery) {
        // validate: chỉ overview + có từ khóa
        if (!AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || searchQuery == null || searchQuery.isBlank()) {
            return List.of();
        }
        // load thí sinh khớp + gắn stage key/label/path
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
        // result
        return hits;
    }
}
