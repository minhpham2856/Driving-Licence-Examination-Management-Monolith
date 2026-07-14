package examstaff.controller.staff.exam.binder;

import examstaff.dto.CandidateDossierViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class CandidateDossierViewBinder {

    private CandidateDossierViewBinder() {
    }

    public static void bind(HttpServletRequest request, CandidateDossierViewDTO view, boolean autoPrint) {
        if (request == null || view == null || view.getProfile() == null) {
            return;
        }
        request.setAttribute("profile", view.getProfile());
        request.setAttribute("examSession", view.getExamSession());
        request.setAttribute("hasPhotoFile", view.isHasPhotoFile());
        request.setAttribute("payment", null);
        if (view.getFees() != null) {
            request.setAttribute("feeLines", view.getFees().getFeeLines());
            request.setAttribute("feeTotal", view.getFees().getFeeTotal());
            request.setAttribute("feesFromPayment", view.getFees().isFeesFromPayment());
        }
        request.setAttribute("dossierTitle", view.getDossierTitle());
        request.setAttribute("dossierSubtitle", view.getDossierSubtitle());
        request.setAttribute("autoPrint", autoPrint);
    }
}
