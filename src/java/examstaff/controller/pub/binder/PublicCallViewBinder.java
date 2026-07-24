package examstaff.controller.pub.binder;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.PublicCallSnapshotDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import examstaff.util.LicenseClassRules;

/**
 * Bind {@link PublicCallSnapshotDTO} sang request attributes cho {@code public-call.jsp}.
 */
public final class PublicCallViewBinder {

    private PublicCallViewBinder() {
    }

    /** Chuẩn hóa mã hạng bằng hiển thị công khai (managed → uppercase fallback). */
    private static String normalizeLicenseForPublicCall(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    /** Normalize licenseCode trên kỳ thi. */
    private static void normalizeExam(ExamSummaryDTO exam) {
        if (exam == null) {
            return;
        }
        exam.setLicenseCode(normalizeLicenseForPublicCall(exam.getLicenseCode()));
    }

    /** Normalize licenseCode trên thí sinh. */
    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) {
            return;
        }
        c.setLicenseCode(normalizeLicenseForPublicCall(c.getLicenseCode()));
    }

    /**
     * Gán attribute JSP: currentExam, calling/next candidate, waitingQueue, cờ pause/end.
     *
     * @param request  request hiện tại
     * @param snapshot snapshot từ {@link examstaff.service.PublicCallQueryService}
     */
    public static void bind(HttpServletRequest request, PublicCallSnapshotDTO snapshot) {
        if (request == null || snapshot == null) {
            return;
        }
        normalizeExam(snapshot.getCurrentExam());
        normalizeCandidate(snapshot.getCallingCandidate());
        normalizeCandidate(snapshot.getNextCandidate());
        if (snapshot.getWaitingQueue() != null) {
            for (ExamRegistrationDTO c : snapshot.getWaitingQueue()) {
                normalizeCandidate(c);
            }
        }

        boolean hasExam = snapshot.getExamId() > 0;
        request.setAttribute("noActiveExam", !hasExam);
        request.setAttribute("examId", hasExam ? snapshot.getExamId() : null);
        request.setAttribute("currentExam", snapshot.getCurrentExam());
        request.setAttribute("callingCandidate", snapshot.getCallingCandidate());
        request.setAttribute("nextCandidate", snapshot.getNextCandidate());
        request.setAttribute("isCallingActive", snapshot.isCallingActive());
        request.setAttribute("shiftEnded", snapshot.isShiftEnded());
        request.setAttribute("examPaused", snapshot.isExamPaused());
        request.setAttribute("waitingQueue", snapshot.getWaitingQueue());
    }
}
