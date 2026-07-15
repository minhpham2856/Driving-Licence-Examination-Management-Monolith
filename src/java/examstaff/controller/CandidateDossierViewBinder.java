package examstaff.controller;

import examstaff.dto.CandidateDossierViewDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind hồ sơ in thí sinh từ {@link CandidateDossierViewDTO} lên request cho JSP dossier.
 */
public final class CandidateDossierViewBinder {

    private CandidateDossierViewBinder() {
    }

    /**
     * Set profile/examSummary/hasPhotoFile/feeLines/feeTotal/feesFromPayment,
     * dossierTitle/Subtitle và {@code autoPrint}.
     */
    public static void bind(HttpServletRequest request, CandidateDossierViewDTO view, boolean autoPrint) {
        if (request == null || view == null || view.getProfile() == null) {
            return;
        }
        request.setAttribute("profile", view.getProfile());
        request.setAttribute("examSummary", view.getExam());
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
