package controller.staff.exam.support;

import dto.examstaff.AllocationStageViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class AllocationStageViewBinder {

    private AllocationStageViewBinder() {
    }

    public static void bind(HttpServletRequest request, AllocationStageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("allocationPracticalStageIds", view.getPracticalStageIds());
        request.setAttribute("allocationNoRoadTestIds", view.getNoRoadTestIds());
        request.setAttribute("allocationStageCounts", view.getStageCounts());
        request.setAttribute("allocationStageList", view.getStageList());
        request.setAttribute("allocationPageSlice", view.getPageSlice());
        request.setAttribute("allocationOverviewHits", view.getOverviewSearchHits());
    }
}
