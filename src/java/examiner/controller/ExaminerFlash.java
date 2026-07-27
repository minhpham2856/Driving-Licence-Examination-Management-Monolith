package examiner.controller;

import examiner.util.ExaminerFlashMessages;
import jakarta.servlet.http.HttpServletRequest;
import shared.Attributes;

// Bind examiner flash query params / editError into request attributes for JSP.
public final class ExaminerFlash {

    private ExaminerFlash() {
    }

    // Read flash params once and set flashSuccess / flashError for the view.
    public static void bind(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        String sbd = request.getParameter("sbd");
        String success = ExaminerFlashMessages.resolveSuccess(
                request.getParameter("successMessage"),
                request.getParameter("saved"),
                request.getParameter("suspended"),
                request.getParameter("unsuspended"),
                sbd,
                request.getParameter("undoSuspended"),
                request.getParameter("invoked"),
                request.getParameter("invokedBatch"),
                request.getParameter("presentDone"),
                request.getParameter("maintenanceDone"),
                request.getParameter("operationalDone"),
                request.getParameter("scoreInvoked"),
                request.getParameter("completeDone"),
                request.getParameter("undoPresent"),
                request.getParameter("wrongInfoDone"),
                request.getParameter("finalized"),
                request.getParameter("signatureMarked"),
                request.getParameter("vehicleChanged"),
                request.getParameter("scoreSaved"));

        String error = ExaminerFlashMessages.resolveError(
                request.getParameter("error"),
                sbd,
                request.getParameter("uploadMessage"));

        if (error == null) {
            Object editError = request.getAttribute(Attributes.Examiner.EDIT_ERROR);
            if (editError instanceof String) {
                String editErrorText = (String) editError;
                if (!editErrorText.isBlank()) {
                    error = editErrorText;
                }
            }
        }

        if (success != null) {
            request.setAttribute(Attributes.Examiner.FLASH_SUCCESS, success);
        }
        if (error != null) {
            request.setAttribute(Attributes.Examiner.FLASH_ERROR, error);
        }
    }
}
