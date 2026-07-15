package examstaff.controller;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.util.JsonUtil;
import examstaff.service.impl.support.shared.LicenseClassRules;
import jakarta.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Bind / serialize snapshot Public Call cho JSP và JSON API (Presentation).
 * Chuẩn hóa hạng GPLX trước khi đưa ra UI/JSON.
 */
public final class PublicCallSnapshotSupport {

    /** Không khởi tạo. */
    private PublicCallSnapshotSupport() {
    }

    /** Chuẩn hóa mã hạng GPLX (managed → trim+upper fallback). */
    private static String normalizeLicense(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    /** Gán lại licenseCode đã chuẩn hóa trên ExamSummaryDTO. */
    private static void normalizeExam(ExamSummaryDTO exam) {
        if (exam != null) {
            exam.setLicenseCode(normalizeLicense(exam.getLicenseCode()));
        }
    }

    /** Gán lại licenseCode đã chuẩn hóa trên thí sinh. */
    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c != null) {
            c.setLicenseCode(normalizeLicense(c.getLicenseCode()));
        }
    }

    /**
     * Chuẩn hóa hạng GPLX trên toàn snapshot (kỳ, đang gọi, kế tiếp, waiting).
     *
     * @param snapshot snapshot public call
     */
    private static void normalizeSnapshot(PublicCallSnapshotDTO snapshot) {
        if (snapshot == null) {
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
    }

    /**
     * Bind thuộc tính JSP public-call từ snapshot (sau normalize).
     *
     * @param request  request JSP
     * @param snapshot dữ liệu trạng thái gọi công khai
     */
    public static void bindRequest(HttpServletRequest request, PublicCallSnapshotDTO snapshot) {
        if (request == null || snapshot == null) {
            return;
        }
        normalizeSnapshot(snapshot);
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

    /**
     * Serialize snapshot thành JSON state (poll API {@code /api/public-call/state}).
     * <p>
     * Luồng: normalize → append meta flags → examDate/deskSbd → calling/next/waitingQueue.
     *
     * @param snapshot snapshot; null → {@code "{}"}
     * @return chuỗi JSON
     */
    public static String toStateJson(PublicCallSnapshotDTO snapshot) {
        if (snapshot == null) {
            return "{}";
        }
        normalizeSnapshot(snapshot);

        // Meta flags + timestamp
        StringBuilder json = new StringBuilder(512);
        json.append('{');
        JsonUtil.appendJsonField(json, "examId", snapshot.getExamId(), true);
        JsonUtil.appendJsonField(json, "isCallingActive", snapshot.isCallingActive(), true);
        JsonUtil.appendJsonField(json, "deskBusy", snapshot.isDeskBusy(), true);
        JsonUtil.appendJsonField(json, "shiftEnded", snapshot.isShiftEnded(), true);
        JsonUtil.appendJsonField(json, "examPaused", snapshot.isExamPaused(), true);
        JsonUtil.appendJsonField(json, "updatedAtMs", snapshot.getUpdatedAtMs(), true);

        // Ngày kỳ + SBD đang ở bàn
        if (snapshot.getCurrentExam() != null && snapshot.getCurrentExam().getExamDate() != null) {
            String examDate = new SimpleDateFormat("dd/MM/yyyy")
                    .format(snapshot.getCurrentExam().getExamDate());
            JsonUtil.appendJsonField(json, "examDate", examDate, true);
        } else {
            json.append("\"examDate\":null,");
        }

        if (snapshot.getDeskSbd() != null && !snapshot.getDeskSbd().isBlank()) {
            JsonUtil.appendJsonField(json, "deskSbd", snapshot.getDeskSbd(), true);
        } else {
            json.append("\"deskSbd\":null,");
        }

        // Đối tượng gọi / kế / hàng chờ
        json.append("\"calling\":");
        JsonUtil.appendCandidateJson(json, snapshot.getCallingCandidate());
        json.append(',');
        json.append("\"next\":");
        JsonUtil.appendCandidateJson(json, snapshot.getNextCandidate());
        json.append(',');
        json.append("\"waitingQueue\":");
        JsonUtil.appendCandidateArrayJson(json, snapshot.getWaitingQueue());
        json.append('}');
        return json.toString();
    }
}
