package registrant.util;

import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.enums.Db2Mappings;
import registrant.enums.ExamRegistrationLifecycleStatus;
import registrant.enums.ExamSessionStatus;
import registrant.enums.ProfileRegistrationStatus;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tiện ích nghiệp vụ thi cho cổng thí sinh: chuẩn hóa hạng GPLX, badge trạng thái, giờ ca thi, validate xung đột lịch.
 * Dùng trên dashboard, my-exams, register-exam; tích hợp ExamSessionStatus, ProfileRegistrationStatus và nhãn nguyện vọng/SBD/chờ kết quả.
 */
public final class RegistrantExamSupport {

    public static final String SBD_PENDING_MESSAGE = "SBD sẽ được cập nhật sau";

    /** Nhãn trạng thái nguyện vọng ngày thi (chờ trung tâm công bố lịch chính thức). */
    public static final String PREFERRED_DATE_STATUS_LABEL = "Nguyện vọng — chờ lịch chính thức";
    /** ExamDates đã Locked: nguyện vọng còn active nhưng không còn mở đăng ký. */
    public static final String PREFERRED_DATE_LOCKED_STATUS_LABEL =
            "Nguyện vọng — đã đóng đăng ký, chờ lịch chính thức";
    /** RegistrationDates IsActive=0 sau khi managing hủy ngày. */
    public static final String PREFERRED_DATE_CANCELLED_STATUS_LABEL = "Nguyện vọng — đã hủy";
    public static final String PREFERRED_DATE_REG_STATUS = "PreferredDate";
    public static final String PREFERRED_DATE_CANCELLED_REG_STATUS = "PreferredDateCancelled";

    /** Đã có SBD / đã xếp ca, chưa đến ngày thi. */
    public static final String SCHEDULED_WAITING_STATUS_LABEL = "Đã xếp lịch — chờ ngày thi";

    /** Đã thi xong phần sát hạch, chưa có ExamResult công bố. */
    public static final String AWAITING_RESULT_STATUS_LABEL = "Đã thi — chờ công bố kết quả";

    public static final int THEORY_MAX_QUESTIONS = 25;
    public static final int THEORY_PASS_CORRECT = 21;
    public static final int PRACTICAL_MAX_SCORE = 100;
    public static final int PRACTICAL_PASS_SCORE = 80;

    private static final SimpleDateFormat ACTIVITY_FMT =
            new SimpleDateFormat("dd/MM/yyyy, HH:mm", new Locale("vi", "VN"));

    private RegistrantExamSupport() {
    }

    /** True nếu CandidateNumber còn dạng PENDING-SBD-*. */
    public static boolean isSbdPending(String candidateNumber) {
        return Db2Mappings.isPendingCandidateNumber(candidateNumber);
    }

    /** Hiển thị SBD; trả thông báo chờ nếu còn pending. */
    public static String formatSbdForDisplay(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank() || isSbdPending(candidateNumber)) {
            return SBD_PENDING_MESSAGE;
        }
        return candidateNumber.trim();
    }

    /** Chuẩn hóa mã hạng (trim + upper). Null/blank → B1. */
    public static String normalizeLicenceClass(String code) {
        if (code == null || code.isBlank()) {
            return "B1";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** Suy loại xe (car/moto) từ mã hạng UI. */
    public static String inferVehicleType(String uiCode) {
        if (uiCode == null || uiCode.isBlank()) {
            return "car";
        }
        String code = uiCode.trim().toUpperCase(Locale.ROOT);
        if (code.startsWith("A")) {
            return "moto";
        }
        if (code.startsWith("D")) {
            return "bus";
        }
        return "car";
    }

    /** Nhãn hiển thị hạng GPLX — khớp mô tả seed DB (A/A1/B1). */
    public static String licenceClassDescription(String uiCode) {
        if (uiCode == null || uiCode.isBlank()) {
            return null;
        }
        return switch (uiCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1" -> "Hạng A1 (Xe mô tô hai bánh đến 125 cm³)";
            case "A", "A2" -> "Hạng A (Xe mô tô hai bánh trên 125 cm³)";
            case "B1" -> "Hạng B1 (Xe mô tô ba bánh)";
            case "B", "B2" -> "Hạng B2 (Ô tô chở người đến 9 chỗ, tải dưới 3.5t)";
            case "C", "C1" -> "Hạng C (Xe tải trên 3.5t)";
            default -> "Hạng " + uiCode.trim().toUpperCase(Locale.ROOT);
        };
    }

    /** CCCD 12 số hoặc CMND 9 số — dùng validate hồ sơ thí sinh. */
    public static boolean isValidGovIdNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String digits = raw.trim().replaceAll("\\s+", "");
        return digits.matches("\\d{12}") || digits.matches("\\d{9}");
    }

    /** Chuẩn hóa CCCD/CMND: trim và bỏ khoảng trắng. */
    public static String normalizeGovIdNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", "");
    }

    /** Badge dashboard: chờ xét duyệt (chưa SBD import) hoặc lịch chính thức (đã SBD); thanh toán khi sát hạch. */
    public static void applyExamStatusBadge(RegistrantRegisteredExamRow row,
            String candidateNumber, String regStatus, String sectionStatus) {
        if ("Completed".equalsIgnoreCase(sectionStatus) || "Passed".equalsIgnoreCase(sectionStatus)) {
            row.setStatusClass("approved");
            row.setStatusLabel("Đã hoàn thành");
            return;
        }
        if ("Rejected".equalsIgnoreCase(regStatus)) {
            row.setStatusClass("rejected");
            row.setStatusLabel("Bị từ chối");
            return;
        }
        if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(regStatus)
                || isSbdPending(candidateNumber)) {
            row.setStatusClass("pending");
            row.setStatusLabel("Chờ xét duyệt");
            return;
        }
        row.setStatusClass("info");
        row.setStatusLabel(SCHEDULED_WAITING_STATUS_LABEL);
    }

    /** Badge nguyện vọng ngày thi trên dashboard (chỉ dòng còn active). */
    public static void applyPreferredDateStatus(RegistrantRegisteredExamRow row) {
        applyPreferredDateStatus(row, true, null);
    }

    /** Badge nguyện vọng dashboard: active + Locked thì đổi nhãn. */
    public static void applyPreferredDateStatus(RegistrantRegisteredExamRow row,
            boolean active, String examDateStatus) {
        row.setPreferredDate(true);
        row.setSbdPending(true);
        row.setSessionTimePublished(false);
        row.setLocation("Theo lịch trung tâm");
        if (!active) {
            row.setStatusClass("rejected");
            row.setStatusLabel(PREFERRED_DATE_CANCELLED_STATUS_LABEL);
            return;
        }
        row.setStatusClass("pending");
        row.setStatusLabel(isLockedExamDateStatus(examDateStatus)
                ? PREFERRED_DATE_LOCKED_STATUS_LABEL
                : PREFERRED_DATE_STATUS_LABEL);
    }

    /** Gán badge/nhãn nguyện vọng ngày thi cho dòng my-exams (active). */
    public static void applyPreferredDateStatus(RegistrantMyExamRow row) {
        applyPreferredDateStatus(row, true, null, null);
    }

    /**
     * Badge my-exams nguyện vọng: active/Open, active/Locked, hoặc đã hủy (IsActive=0).
     */
    public static void applyPreferredDateStatus(RegistrantMyExamRow row, boolean active,
            String examDateStatus, String cancelReason) {
        row.setPreferredDate(true);
        row.setPreferredCancelled(!active);
        row.setCancelReason(blankToNull(cancelReason));
        row.setSbdPending(true);
        row.setSbdDisplay("—");
        row.setRoomName("Chưa xếp phòng");
        row.setSessionTimePublished(false);
        row.setPendingPayment(false);
        if (!active) {
            row.setStatusClass("rejected");
            row.setStatusLabel(PREFERRED_DATE_CANCELLED_STATUS_LABEL);
            row.setRegistrationStatus(PREFERRED_DATE_CANCELLED_REG_STATUS);
            String reason = blankToNull(cancelReason);
            row.setOverallResultLabel(reason != null
                    ? "Ngày thi nguyện vọng đã bị hủy. Lý do: " + reason
                    : "Ngày thi nguyện vọng đã bị hủy. Bạn có thể chọn ngày khác đang mở.");
            row.setSessionTimeDisplay("Đã hủy");
            return;
        }
        row.setStatusClass("pending");
        boolean locked = isLockedExamDateStatus(examDateStatus);
        row.setStatusLabel(locked ? PREFERRED_DATE_LOCKED_STATUS_LABEL : PREFERRED_DATE_STATUS_LABEL);
        row.setRegistrationStatus(PREFERRED_DATE_REG_STATUS);
        row.setOverallResultLabel(locked
                ? "Đã đóng đăng ký — lựa chọn được giữ, chờ trung tâm công bố lịch chính thức"
                : "Đã gửi nguyện vọng — chờ trung tâm công bố lịch chính thức");
        row.setSessionTimeDisplay("Chờ trung tâm công bố");
    }

    /** True nếu ExamDates.Status = Locked. */
    public static boolean isLockedExamDateStatus(String status) {
        return status != null && "Locked".equalsIgnoreCase(status.trim());
    }

    /** True nếu dòng my-exams là nguyện vọng còn hiệu lực (chưa bị hủy). */
    public static boolean isActivePreferredMyExam(RegistrantMyExamRow row) {
        return row != null && row.isPreferredDate() && !row.isPreferredCancelled();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** Badge my-exams: nguyện vọng / chờ SBD → đã xếp lịch → đã thi chờ kết quả → đạt/trượt. */
    public static void applyMyExamStatus(RegistrantMyExamRow row, String candidateNumber,
            String regStatus, String sectionStatus, Integer overallPassed) {
        if (overallPassed != null && overallPassed == 1) {
            row.setStatusClass("approved");
            row.setStatusLabel("Đạt");
            row.setOverallResultLabel("Đạt");
        } else if (overallPassed != null && overallPassed == 0) {
            row.setStatusClass("rejected");
            row.setStatusLabel("Trượt");
            row.setOverallResultLabel("Trượt");
        } else if (ExamRegistrationLifecycleStatus.REGISTRATION_REJECTED.equalsIgnoreCase(regStatus)
                || ExamRegistrationLifecycleStatus.CANCELLED.equalsIgnoreCase(regStatus)) {
            row.setStatusClass("rejected");
            row.setStatusLabel(ExamRegistrationLifecycleStatus.toDisplayLabel(regStatus));
            row.setOverallResultLabel("—");
        } else if (ExamRegistrationLifecycleStatus.isCancellationPending(regStatus)) {
            row.setStatusClass("pending");
            row.setStatusLabel("Chờ hủy đăng ký");
            row.setOverallResultLabel("Chưa có SBD");
        } else if (ProfileRegistrationStatus.PENDING.equalsIgnoreCase(regStatus)
                || ExamRegistrationLifecycleStatus.PRE_REGISTERED.equalsIgnoreCase(regStatus)
                || isSbdPending(candidateNumber)) {
            row.setStatusClass("pending");
            row.setStatusLabel("Chờ xét duyệt");
            row.setOverallResultLabel("Chưa có SBD");
        } else if ("Completed".equalsIgnoreCase(sectionStatus)) {
            row.setStatusClass("info");
            row.setStatusLabel(AWAITING_RESULT_STATUS_LABEL);
            row.setOverallResultLabel("Đã hoàn thành phần thi — chờ trung tâm công bố kết quả");
        } else {
            row.setStatusClass("info");
            row.setStatusLabel(SCHEDULED_WAITING_STATUS_LABEL);
            row.setOverallResultLabel("Đã có lịch chính thức — chờ đến ngày thi");
        }
    }

    /** Gán nhãn điểm chi tiết cho thí sinh (lý thuyết / thực hành / đường trường). */
    public static void applyScorePresentation(RegistrantMyExamRow row,
            String theoryPassedDb, String practicalPassedDb) {
        applyTheoryPresentation(row, theoryPassedDb);
        applyPracticalPresentation(row, practicalPassedDb);
        applyRoadPresentation(row);
    }

    private static void applyTheoryPresentation(RegistrantMyExamRow row, String theoryPassedDb) {
        Integer score = row.getTheoryScore();
        if (score == null) {
            return;
        }
        Boolean passed = resolvePassedFlag(theoryPassedDb);
        int correct = score <= THEORY_MAX_QUESTIONS
                ? score
                : Math.round(score * THEORY_MAX_QUESTIONS / 100f);
        correct = Math.max(0, Math.min(THEORY_MAX_QUESTIONS, correct));
        int wrong = Math.max(0, THEORY_MAX_QUESTIONS - correct);

        row.setTheoryCorrectCount(correct);
        row.setTheoryWrongCount(wrong);
        row.setTheoryScoreDisplay(correct + "/" + THEORY_MAX_QUESTIONS);
        row.setTheoryScoreDetail(wrong == 0
                ? "Trả lời đúng tất cả " + THEORY_MAX_QUESTIONS + " câu"
                : wrong + " câu sai · Điểm chuẩn " + THEORY_PASS_CORRECT + "/" + THEORY_MAX_QUESTIONS + " câu đúng");

        if (passed == null) {
            passed = correct >= THEORY_PASS_CORRECT;
        }
        applyPassBadge(row, passed, true);
    }

    private static void applyPracticalPresentation(RegistrantMyExamRow row, String practicalPassedDb) {
        Integer score = row.getPracticalScore();
        if (score == null) {
            return;
        }
        row.setPracticalScoreDisplay(score + "/" + PRACTICAL_MAX_SCORE);
        int deducted = Math.max(0, PRACTICAL_MAX_SCORE - score);
        row.setPracticalScoreDetail(deducted == 0
                ? "Không bị trừ điểm"
                : "Bị trừ " + deducted + " điểm · Điểm chuẩn " + PRACTICAL_PASS_SCORE + "/" + PRACTICAL_MAX_SCORE);
        Boolean passed = resolvePassedFlag(practicalPassedDb);
        if (passed == null) {
            passed = score >= PRACTICAL_PASS_SCORE;
        }
        applyPassBadge(row, passed, false);
    }

    private static void applyRoadPresentation(RegistrantMyExamRow row) {
        Integer score = row.getRoadScore();
        if (score == null) {
            return;
        }
        row.setRoadScoreDisplay(score + "/" + PRACTICAL_MAX_SCORE);
        int deducted = Math.max(0, PRACTICAL_MAX_SCORE - score);
        row.setRoadScoreDetail(deducted == 0
                ? "Không bị trừ điểm"
                : "Bị trừ " + deducted + " điểm · Điểm chuẩn " + PRACTICAL_PASS_SCORE + "/" + PRACTICAL_MAX_SCORE);
        applyPassBadge(row, score >= PRACTICAL_PASS_SCORE, null);
    }

    private static Boolean resolvePassedFlag(String dbValue) {
        if (dbValue == null || dbValue.isBlank() || "none".equalsIgnoreCase(dbValue.trim())) {
            return null;
        }
        if ("passed".equalsIgnoreCase(dbValue.trim())) {
            return Boolean.TRUE;
        }
        if ("failed".equalsIgnoreCase(dbValue.trim())) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static void applyPassBadge(RegistrantMyExamRow row, boolean passed, Boolean theorySection) {
        String label = passed ? "Đạt" : "Trượt";
        String badgeClass = passed ? "passed" : "failed";
        if (theorySection == null) {
            row.setRoadPassBadgeClass(badgeClass);
            return;
        }
        if (theorySection) {
            row.setTheoryResultLabel(label);
            row.setTheoryPassBadgeClass(badgeClass);
        } else {
            row.setPracticalResultLabel(label);
            row.setPracticalPassBadgeClass(badgeClass);
        }
    }

    /** Công bố giờ ca thi sau khi có điểm hoặc ca đã diễn ra — phòng Session status chưa đồng bộ. */
    public static void finalizeSessionTimeDisplay(RegistrantMyExamRow row, String sessionStatus,
            java.util.Date sessionStart, java.util.Date sessionEnd, String sectionStatus) {
        if (!row.isSessionTimePublished() && shouldRevealSessionTime(row, sessionStatus, sectionStatus)) {
            row.setSessionTimePublished(true);
            row.setSessionStart(sessionStart);
            row.setSessionEnd(sessionEnd);
        }
        if (row.isSessionTimePublished()) {
            row.setSessionTimeDisplay(formatSessionTimeRange(row.getSessionStart(), row.getSessionEnd()));
        }
    }

    private static boolean shouldRevealSessionTime(RegistrantMyExamRow row,
            String sessionStatus, String sectionStatus) {
        if (isSessionTimePublished(sessionStatus)) {
            return true;
        }
        if (sectionStatus != null) {
            String s = sectionStatus.trim();
            if ("Completed".equalsIgnoreCase(s) || "Passed".equalsIgnoreCase(s)
                    || "Failed".equalsIgnoreCase(s)) {
                return true;
            }
        }
        return row.getTheoryScore() != null
                || row.getPracticalScore() != null
                || row.getRoadScore() != null
                || "Đạt".equals(row.getOverallResultLabel())
                || "Trượt".equals(row.getOverallResultLabel());
    }

    /** Format Timestamp hoạt động dashboard theo locale VI. */
    public static String formatActivityTime(Timestamp ts) {
        if (ts == null) {
            return "Vừa xong";
        }
        Calendar cal = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(ts);
        if (isSameDay(cal, target)) {
            return "Hôm nay, " + new SimpleDateFormat("HH:mm", Locale.ROOT).format(ts);
        }
        cal.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(cal, target)) {
            return "Hôm qua, " + new SimpleDateFormat("HH:mm", Locale.ROOT).format(ts);
        }
        return ACTIVITY_FMT.format(ts);
    }

    /** Giờ ca thi chỉ công bố sau khi cán bộ coi thi bấm "Bắt đầu ca thi". */
    public static boolean isSessionTimePublished(String sessionStatus) {
        return ExamSessionStatus.isSessionInProgress(sessionStatus) || ExamSessionStatus.isSessionEnded(sessionStatus);
    }

    /** Gán giờ ca thi lên row nếu session đã công bố. */
    public static void applyPublishedSessionSchedule(RegistrantRegisteredExamRow row, String sessionStatus,
            java.util.Date sessionStart, java.util.Date sessionEnd) {
        boolean published = isSessionTimePublished(sessionStatus);
        row.setSessionTimePublished(published);
        if (!published) {
            row.setSessionStart(null);
            row.setSessionEnd(null);
            return;
        }
        row.setSessionStart(sessionStart);
        row.setSessionEnd(sessionEnd);
    }

    /** Gán giờ ca thi + chuỗi hiển thị cho my-exam row. */
    public static void applyPublishedSessionSchedule(RegistrantMyExamRow row, String sessionStatus,
            java.util.Date sessionStart, java.util.Date sessionEnd) {
        boolean published = isSessionTimePublished(sessionStatus);
        row.setSessionTimePublished(published);
        if (!published) {
            row.setSessionStart(null);
            row.setSessionEnd(null);
            row.setSessionTimeDisplay(null);
            return;
        }
        row.setSessionStart(sessionStart);
        row.setSessionEnd(sessionEnd);
        row.setSessionTimeDisplay(formatSessionTimeRange(sessionStart, sessionEnd));
    }

    /** Định dạng giờ bắt đầu ca thi (vd: "08:00") — không hiện giờ kết thúc. */
    public static String formatSessionTimeRange(java.util.Date start, java.util.Date end) {
        if (start == null && end == null) {
            return null;
        }
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.ROOT);
        return start != null ? hm.format(start) : hm.format(end);
    }

    /** Đọc cột INT/BIT an toàn — tương thích mọi JDBC driver (tránh pattern matching gây lỗi compile NB). */
    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        return null;
    }

    private static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
