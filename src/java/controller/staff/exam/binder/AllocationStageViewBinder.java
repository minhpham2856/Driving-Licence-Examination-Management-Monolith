package controller.staff.exam.binder;

import dto.examstaff.AllocationStageViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class AllocationStageViewBinder {

    private AllocationStageViewBinder() {
    }

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
