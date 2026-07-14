package registrant.util;

import registrant.enums.ExamRegistrationLifecycleStatus;
import registrant.enums.ProfileRegistrationStatus;
import registrant.dao.impl.DocumentDAOImpl;
import registrant.dto.RegistrantDocumentSummary;
import registrant.dto.RegistrantProfileProgressStep;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.dto.RegistrantTrackingLog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Timeline track-profile 5 bước (Tiếp nhận→Duyệt→Bổ sung→Dã duyệt→SBD); resolveProgressIndex từ RegistrationStatus + docs + SBD. */
public final class RegistrantProfileProgressBuilder {

    private static final String[] STEP_KEYS = {
            "receive", "review", "supplement", "approved", "sbd"
    };
    private static final String[] STEP_TITLES = {
            "Tiếp nhận hồ sơ",
            "Đang duyệt",
            "Yêu cầu bổ sung",
            "Đã duyệt",
            "Đã cấp SBD"
    };
    private static final String[] STEP_ICONS = {
            "document", "review", "supplement", "approved", "sbd"
    };

    private RegistrantProfileProgressBuilder() {
    }

    public static List<RegistrantProfileProgressStep> build(
            String registrationStatus,
            RegistrantDocumentSummary documentSummary,
            List<RegistrantTrackingLog> trackingLogs,
            List<RegistrantRegisteredExamRow> registeredExams) {

        String status = registrationStatus != null ? registrationStatus.trim() : ProfileRegistrationStatus.DRAFT;
        boolean docsApproved = ProfileRegistrationStatus.APPROVED.equalsIgnoreCase(status)
                || isExamLifecyclePastDocuments(status);

        boolean wasRejected = ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status)
                || findLog(trackingLogs, RegistrantTrackingCategories.DOCUMENT_REJECT) != null;

        OfficialSbdInfo sbdInfo = resolveOfficialSbd(registeredExams);
        int progressIndex = resolveProgressIndex(status, documentSummary, wasRejected, docsApproved, sbdInfo.assigned);

        List<RegistrantProfileProgressStep> steps = new ArrayList<>(STEP_KEYS.length);
        for (int i = 0; i < STEP_KEYS.length; i++) {
            steps.add(buildStep(i, progressIndex, status, documentSummary, trackingLogs, sbdInfo, wasRejected));
        }
        return steps;
    }

    /** Chỉ số bước active 0..4 (≥length = có SBD); ưu tiên SBD→Approved→Rejected→Pending→Draft. */
    private static int resolveProgressIndex(String status, RegistrantDocumentSummary summary,
            boolean wasRejected, boolean docsApproved, boolean hasOfficialSbd) {
        if (hasOfficialSbd) {
            return STEP_KEYS.length;
        }
        if (docsApproved || isExamLifecyclePastDocuments(status)) {
            return 4;
        }
        if (ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status) || wasRejected) {
            return 2;
        }
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            return 1;
        }
        if (ProfileRegistrationStatus.DRAFT.equalsIgnoreCase(status)) {
            if (summary != null && summary.getRequiredUploaded() >= summary.getRequiredTotal()
                    && summary.getAwaitingSubmitCount() == 0) {
                return 1;
            }
            return 0;
        }
        return 1;
    }

    private static boolean isExamLifecyclePastDocuments(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String s = status.trim();
        return ExamRegistrationLifecycleStatus.PRE_REGISTERED.equalsIgnoreCase(s)
                || ExamRegistrationLifecycleStatus.CHECKED_IN.equalsIgnoreCase(s)
                || ExamRegistrationLifecycleStatus.PRESENT.equalsIgnoreCase(s)
                || ExamRegistrationLifecycleStatus.COMPLETED.equalsIgnoreCase(s)
                || ExamRegistrationLifecycleStatus.CANCEL_REQUESTED.equalsIgnoreCase(s);
    }

    private static RegistrantProfileProgressStep buildStep(int index, int progressIndex, String status,
            RegistrantDocumentSummary summary, List<RegistrantTrackingLog> logs,
            OfficialSbdInfo sbdInfo, boolean wasRejected) {

        RegistrantProfileProgressStep step = new RegistrantProfileProgressStep();
        step.setStepKey(STEP_KEYS[index]);
        step.setTitle(STEP_TITLES[index]);
        step.setIcon(STEP_ICONS[index]);

        if (index < progressIndex) {
            step.setState(RegistrantProfileProgressStep.STATE_COMPLETED);
        } else if (index == progressIndex) {
            step.setState(RegistrantProfileProgressStep.STATE_ACTIVE);
        } else {
            step.setState(RegistrantProfileProgressStep.STATE_PENDING);
        }

        switch (index) {
            case 0 -> applyReceiveStep(step, logs, summary);
            case 1 -> applyReviewStep(step, logs, status);
            case 2 -> applySupplementStep(step, logs, status, wasRejected);
            case 3 -> applyApprovedStep(step, logs);
            case 4 -> applySbdStep(step, sbdInfo, status);
            default -> { }
        }
        return step;
    }

    private static void applyReceiveStep(RegistrantProfileProgressStep step,
            List<RegistrantTrackingLog> logs, RegistrantDocumentSummary summary) {
        RegistrantTrackingLog log = firstOf(logs,
                RegistrantTrackingCategories.PROFILE,
                RegistrantTrackingCategories.DOCUMENT_UPLOAD,
                RegistrantTrackingCategories.DOCUMENT_SUBMIT);
        if (log != null) {
            step.setTimestamp(log.getTimestamp());
        }
        if (step.isCompleted()) {
            step.setDescription(log != null && log.getRemarks() != null && !log.getRemarks().isBlank()
                    ? log.getRemarks()
                    : "Hồ sơ đăng ký dự thi đã được tiếp nhận và lưu trên hệ thống Lái Vui.");
            step.setFooterType("shield");
            step.setFooterText("Phụ trách: " + abbreviateActor(log != null ? log.getActorRole() : "Hệ thống"));
        } else if (step.isActive()) {
            int uploaded = summary != null ? summary.getRequiredUploaded() : 0;
            int total = summary != null ? summary.getRequiredTotal() : 4;
            step.setDescription(String.format(
                    "Đang hoàn thiện giấy tờ bắt buộc (%d/%d). Vui lòng tải đủ tài liệu trước khi gửi duyệt.",
                    uploaded, total));
            step.setFooterType("clock");
            step.setFooterText("Hoàn tất tài liệu để chuyển bước duyệt");
            step.setStatusHint("Đang xử lý");
        } else {
            step.setStatusHint("Chưa tới bước này");
        }
    }

    private static void applyReviewStep(RegistrantProfileProgressStep step,
            List<RegistrantTrackingLog> logs, String status) {
        RegistrantTrackingLog log = findLog(logs, RegistrantTrackingCategories.DOCUMENT_SUBMIT);
        if (log == null && ProfileRegistrationStatus.PENDING.equalsIgnoreCase(status)) {
            log = findByTitleContains(logs, "chờ duyệt");
        }
        if (log != null) {
            step.setTimestamp(log.getTimestamp());
        }
        if (step.isCompleted()) {
            step.setDescription("Ban quản lý đã tiếp nhận hồ sơ và hoàn tất kiểm tra sơ bộ.");
            step.setFooterType("shield");
            step.setFooterText("Phụ trách: " + abbreviateActor(log != null ? log.getActorRole() : "Ban quản lý"));
        } else if (step.isActive()) {
            step.setDescription("Sở GTVT / Ban sát hạch đang đối chiếu giấy tờ, ảnh chân dung và thông tin cá nhân.");
            step.setFooterType("clock");
            step.setFooterText("Dự kiến hoàn thành trong 48h tới");
            if (step.getTimestamp() == null) {
                step.setTimestamp(new Date());
            }
        } else {
            step.setStatusHint("Chưa tới bước này");
        }
    }

    private static void applySupplementStep(RegistrantProfileProgressStep step,
            List<RegistrantTrackingLog> logs, String status, boolean wasRejected) {
        RegistrantTrackingLog log = findLog(logs, RegistrantTrackingCategories.DOCUMENT_REJECT);

        if (ProfileRegistrationStatus.REJECTED.equalsIgnoreCase(status)) {
            step.setState(RegistrantProfileProgressStep.STATE_ACTIVE);
            if (log != null) {
                step.setTimestamp(log.getTimestamp());
            }
            step.setDescription(log != null && log.getRemarks() != null && !log.getRemarks().isBlank()
                    ? log.getRemarks()
                    : "Hồ sơ cần bổ sung tài liệu hoặc chỉnh sửa thông tin trước khi tiếp tục xét duyệt.");
            step.setFooterType("clock");
            step.setFooterText("Vui lòng upload bổ sung trong mục Tài liệu đính kèm");
            return;
        }

        if (step.isCompleted() && wasRejected) {
            if (log != null) {
                step.setTimestamp(log.getTimestamp());
            }
            step.setDescription("Yêu cầu bổ sung đã được xử lý. Hồ sơ chuyển sang bước duyệt tiếp theo.");
            step.setFooterType("shield");
            step.setFooterText("Phụ trách: Thí sinh / Ban quản lý");
            return;
        }

        if (step.isPending()) {
            step.setStatusHint("Chưa tới bước này");
            step.setPlaceholder(true);
            step.setDescription("Thông tin yêu cầu bổ sung (nếu có) sẽ hiển thị tại đây.");
        }
    }

    private static void applyApprovedStep(RegistrantProfileProgressStep step, List<RegistrantTrackingLog> logs) {
        RegistrantTrackingLog log = findBestApprovalLog(logs);
        if (log != null) {
            step.setTimestamp(log.getTimestamp());
        }
        if (step.isCompleted()) {
            step.setDescription(formatApprovedDescription(log));
            step.setFooterType("shield");
            step.setFooterText("Phụ trách: " + abbreviateActor(log != null ? log.getActorRole() : "Ban quản lý"));
        } else if (step.isActive()) {
            step.setDescription("Đang chờ xác nhận phê duyệt cuối cùng từ Ban quản lý.");
            step.setFooterType("clock");
            step.setFooterText("Dự kiến hoàn thành trong 24h tới");
            step.setStatusHint("Đang chờ");
        } else {
            step.setStatusHint("Đang chờ");
        }
    }

    private static void applySbdStep(RegistrantProfileProgressStep step,
            OfficialSbdInfo sbdInfo, String status) {
        if (sbdInfo.assigned) {
            step.setState(RegistrantProfileProgressStep.STATE_COMPLETED);
            step.setTimestamp(sbdInfo.assignedAt);
            step.setDescription(String.format(
                    "Số báo danh chính thức: %s. Bạn có thể tra cứu lịch thi tại mục Lịch thi & kết quả.",
                    sbdInfo.displaySbd));
            step.setFooterType("shield");
            step.setFooterText("Phụ trách: Ban sát hạch");
        } else if (step.isActive()) {
            boolean registered = isExamLifecyclePastDocuments(status)
                    || ExamRegistrationLifecycleStatus.PRE_REGISTERED.equalsIgnoreCase(status);
            step.setDescription(registered
                    ? "Đăng ký ca thi đã được ghi nhận. Ban sát hạch sẽ cấp SBD khi công bố danh sách chính thức."
                    : "Sau khi đăng ký ca thi, SBD sẽ được cập nhật khi Ban sát hạch import danh sách thí sinh.");
            step.setFooterType("clock");
            step.setFooterText("Theo dõi tại Lịch thi & kết quả");
            step.setStatusHint("Đang chờ");
        } else {
            step.setStatusHint("Chưa tới bước này");
        }
    }

    private static OfficialSbdInfo resolveOfficialSbd(List<RegistrantRegisteredExamRow> exams) {
        OfficialSbdInfo info = new OfficialSbdInfo();
        if (exams == null) {
            return info;
        }
        for (RegistrantRegisteredExamRow exam : exams) {
            String raw = exam.getCandidateNumber();
            if (raw != null && !RegistrantExamSupport.isSbdPending(raw)) {
                info.assigned = true;
                info.displaySbd = RegistrantExamSupport.formatSbdForDisplay(raw);
                info.assignedAt = exam.getExamDate();
                return info;
            }
        }
        return info;
    }

    private static final String DEFAULT_APPROVED_DESCRIPTION =
            "Tất cả giấy tờ bắt buộc đã được phê duyệt. Bạn có thể đăng ký ca thi sát hạch.";

    /** Ưu tiên log duyệt cấp hồ sơ (ExamRegistration), không lấy metadata tài liệu còn #PENDING#. */
    private static RegistrantTrackingLog findBestApprovalLog(List<RegistrantTrackingLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        RegistrantTrackingLog profileLevel = logs.stream()
                .filter(l -> RegistrantTrackingCategories.DOCUMENT_APPROVE.equals(l.getCategory()))
                .filter(l -> l.getEventTitle() != null
                        && l.getEventTitle().toLowerCase(Locale.ROOT).contains("phê duyệt hồ sơ"))
                .max(Comparator.comparing(RegistrantTrackingLog::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (profileLevel != null) {
            return profileLevel;
        }
        return logs.stream()
                .filter(l -> RegistrantTrackingCategories.DOCUMENT_APPROVE.equals(l.getCategory()))
                .filter(l -> !containsInternalMarkers(l.getRemarks()))
                .max(Comparator.comparing(RegistrantTrackingLog::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(findLog(logs, RegistrantTrackingCategories.DOCUMENT_APPROVE));
    }

    private static String formatApprovedDescription(RegistrantTrackingLog log) {
        if (log == null || log.getRemarks() == null || log.getRemarks().isBlank()) {
            return DEFAULT_APPROVED_DESCRIPTION;
        }
        String remarks = DocumentDAOImpl.stripInternalMarkers(log.getRemarks());
        if (remarks.isBlank() || containsInternalMarkers(log.getRemarks())) {
            return DEFAULT_APPROVED_DESCRIPTION;
        }
        return remarks;
    }

    private static boolean containsInternalMarkers(String text) {
        return text != null
                && (text.contains(DocumentDAOImpl.MARK_PENDING)
                || text.contains("Gửi yêu cầu duyệt hồ sơ"));
    }

    private static RegistrantTrackingLog findLog(List<RegistrantTrackingLog> logs, String category) {
        if (logs == null) {
            return null;
        }
        return logs.stream()
                .filter(l -> category.equals(l.getCategory()))
                .max(Comparator.comparing(RegistrantTrackingLog::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private static RegistrantTrackingLog firstOf(List<RegistrantTrackingLog> logs, String... categories) {
        RegistrantTrackingLog best = null;
        for (String cat : categories) {
            RegistrantTrackingLog log = findLog(logs, cat);
            if (log == null) {
                continue;
            }
            if (best == null || (log.getTimestamp() != null && best.getTimestamp() != null
                    && log.getTimestamp().before(best.getTimestamp()))) {
                best = log;
            }
        }
        return best;
    }

    private static RegistrantTrackingLog findByTitleContains(List<RegistrantTrackingLog> logs, String fragment) {
        if (logs == null || fragment == null) {
            return null;
        }
        String needle = fragment.toLowerCase(Locale.ROOT);
        return logs.stream()
                .filter(l -> l.getEventTitle() != null
                        && l.getEventTitle().toLowerCase(Locale.ROOT).contains(needle))
                .findFirst()
                .orElse(null);
    }

    private static String abbreviateActor(String actor) {
        if (actor == null || actor.isBlank()) {
            return "Ban quản lý";
        }
        String trimmed = actor.trim();
        if (trimmed.length() <= 24) {
            return trimmed;
        }
        return trimmed.substring(0, 21) + "...";
    }

    private static final class OfficialSbdInfo {
        boolean assigned;
        String displaySbd;
        Date assignedAt;
    }
}
