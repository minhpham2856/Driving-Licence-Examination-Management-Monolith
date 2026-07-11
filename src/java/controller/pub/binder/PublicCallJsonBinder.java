package controller.pub.binder;

import dto.examstaff.PublicCallSnapshotDTO;
import util.JsonUtil;
import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;

import java.text.SimpleDateFormat;
import java.util.Locale;
import util.examstaff.LicenseClassRules;

public final class PublicCallJsonBinder {

    private PublicCallJsonBinder() {
    }

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

    private static void normalizeExam(ExamSummaryDTO exam) {
        if (exam == null) {
            return;
        }
        exam.setLicenseCode(normalizeLicenseForPublicCall(exam.getLicenseCode()));
    }

    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) {
            return;
        }
        c.setLicenseCode(normalizeLicenseForPublicCall(c.getLicenseCode()));
    }

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
