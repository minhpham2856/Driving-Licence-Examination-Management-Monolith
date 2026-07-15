package examstaff.controller;

import examstaff.dto.AllocationStageViewDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind dữ liệu danh sách phân bổ theo giai đoạn từ {@link AllocationStageViewDTO}.
 */
public final class AllocationStageViewBinder {

    private AllocationStageViewBinder() {
    }

    /**
     * Set {@code allocationStageCounts/List}, {@code allocationPageSlice}, {@code allocationOverviewHits}.
     */
    public static void bind(HttpServletRequest request, AllocationStageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("allocationStageCounts", view.getStageCounts());
        request.setAttribute("allocationStageList", view.getStageList());
        request.setAttribute("allocationPageSlice", view.getPageSlice());
        request.setAttribute("allocationOverviewHits", view.getOverviewSearchHits());
    }
}
