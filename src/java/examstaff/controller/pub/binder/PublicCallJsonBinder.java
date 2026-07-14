package examstaff.controller.pub.binder;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.util.JsonUtil;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

import java.text.SimpleDateFormat;
import java.util.Locale;
import examstaff.util.LicenseClassRules;

/**
 * Serialize {@link PublicCallSnapshotDTO} thành JSON cho API {@code /api/public-call/state}.
 */
public final class PublicCallJsonBinder {

    private PublicCallJsonBinder() {
    }

    /** Chuẩn hóa mã hạng bằng trong payload JSON. */
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

    /** Normalize licenseCode trên kỳ thi trước khi ghi JSON. */
    private static void normalizeExam(ExamSummaryDTO exam) {
        if (exam == null) {
            return;
        }
        exam.setLicenseCode(normalizeLicenseForPublicCall(exam.getLicenseCode()));
    }

    /** Normalize licenseCode trên thí sinh trước khi ghi JSON. */
    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) {
            return;
        }
        c.setLicenseCode(normalizeLicenseForPublicCall(c.getLicenseCode()));
    }

    /**
     * Đóng snapshot thành chuỗi JSON (calling, next, waitingQueue, desk, flags).
     *
     * @param snapshot snapshot hiện tại (null → {@code {}})
     * @return JSON UTF-8 string
     */
    public static String toStateJson(PublicCallSnapshotDTO snapshot) {
        if (snapshot == null) {
            return "{}";
        }

        normalizeExam(snapshot.getCurrentExam());
        normalizeCandidate(snapshot.getCallingCandidate());
        normalizeCandidate(snapshot.getNextCandidate());
        if (snapshot.getWaitingQueue() != null) {
            for (ExamRegistrationDTO c : snapshot.getWaitingQueue()) {
                normalizeCandidate(c);
            }
        }

        StringBuilder json = new StringBuilder(512);
        json.append('{');
        JsonUtil.appendJsonField(json, "examId", snapshot.getExamId(), true);
        JsonUtil.appendJsonField(json, "isCallingActive", snapshot.isCallingActive(), true);
        JsonUtil.appendJsonField(json, "deskBusy", snapshot.isDeskBusy(), true);
        JsonUtil.appendJsonField(json, "shiftEnded", snapshot.isShiftEnded(), true);
        JsonUtil.appendJsonField(json, "examPaused", snapshot.isExamPaused(), true);
        JsonUtil.appendJsonField(json, "updatedAtMs", snapshot.getUpdatedAtMs(), true);

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
