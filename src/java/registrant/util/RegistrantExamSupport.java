package registrant.util;

import registrant.dao.ExamRegistrationDAO;
import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.dto.RegistrantSectionRegistrationBlock;
import registrant.dto.exam.SessionExamSectionInfo;
import registrant.dto.exam.SessionScheduleInfo;
import registrant.enums.Db2Mappings;
import registrant.enums.ExamRegistrationLifecycleStatus;
import registrant.enums.ExamSessionStatus;
import registrant.enums.ProfileRegistrationStatus;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tiện ích dùng chung cho luồng thí sinh: ánh xạ mã GPLX UI ↔ DB,
 * gán nhãn trạng thái, định dạng thời gian hiển thị.
 */
public final class RegistrantExamSupport {

    public static final String SBD_PENDING_MESSAGE = "SBD sẽ được cập nhật sau";

    public static final int THEORY_MAX_QUESTIONS = 35;
    public static final int THEORY_PASS_CORRECT = 32;
    public static final int THEORY_PASS_PERCENT = 80;
    public static final int PRACTICAL_MAX_SCORE = 100;
    public static final int PRACTICAL_PASS_SCORE = 80;

    private static final SimpleDateFormat ACTIVITY_FMT =
            new SimpleDateFormat("dd/MM/yyyy, HH:mm", new Locale("vi", "VN"));

    private RegistrantExamSupport() {
    }

    public static boolean isSbdPending(String candidateNumber) {
        return Db2Mappings.isPendingCandidateNumber(candidateNumber);
    }

    public static String formatSbdForDisplay(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank() || isSbdPending(candidateNumber)) {
            return SBD_PENDING_MESSAGE;
        }
        return candidateNumber.trim();
    }

    /** UI dùng B2/A2 trong khi DB seed dùng B/A. */
    public static String toDbLicenceCode(String uiCode) {
        if (uiCode == null) {
            return "B";
        }
        return switch (uiCode.trim().toUpperCase(Locale.ROOT)) {
            case "B2" -> "B";
            case "A2" -> "A";
            default -> uiCode.trim().toUpperCase(Locale.ROOT);
        };
    }

    public static String toUiLicenceCode(String dbCode) {
        if (dbCode == null) {
            return "B2";
        }
        return switch (dbCode.trim().toUpperCase(Locale.ROOT)) {
            case "B" -> "B2";
            case "A" -> "A2";
            default -> dbCode.trim().toUpperCase(Locale.ROOT);
        };
    }

    /** Lệ phí mặc định khớp mock trên register-exam.jsp khi chưa tính từ bảng Fee. */
    public static long defaultExamFee(String uiCode) {
        if (uiCode == null) {
            return 1_200_000L;
        }
        return switch (uiCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1" -> 250_000L;
            case "A2" -> 350_000L;
            case "B1" -> 800_000L;
            case "B2" -> 1_200_000L;
            case "C1" -> 1_500_000L;
            case "C" -> 2_000_000L;
            case "D1", "D2" -> 2_500_000L;
            case "D" -> 3_000_000L;
            default -> 1_200_000L;
        };
    }

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

    /** Nhãn hiển thị hạng GPLX trên hồ sơ / đăng ký thi. */
    public static String licenceClassDescription(String uiCode) {
        if (uiCode == null || uiCode.isBlank()) {
            return null;
        }
        return switch (uiCode.trim().toUpperCase(Locale.ROOT)) {
            case "A1" -> "Hạng A1 (Mô tô 2 bánh dưới 175cc)";
            case "A2" -> "Hạng A2 (Mô tô 2 bánh trên 175cc)";
            case "B1" -> "Hạng B1 (Ô tô số tự động)";
            case "B2" -> "Hạng B2 (Ô tô chở người đến 9 chỗ, tải dưới 3.5t)";
            case "C", "C1" -> "Hạng C (Xe tải trên 3.5t)";
            default -> "Hạng " + uiCode.trim().toUpperCase(Locale.ROOT);
        };
    }

    /** CCCD 12 số hoặc CMND 9 số - dùng validate hồ sơ thí sinh. */
    public static boolean isValidGovIdNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String digits = raw.trim().replaceAll("\\s+", "");
        return digits.matches("\\d{12}") || digits.matches("\\d{9}");
    }

    public static String normalizeGovIdNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", "");
    }

    /**
     * Gán badge trạng thái cho bảng dashboard: chờ xét duyệt (chưa có SBD import)
     * hoặc lịch thi chính thức (đã có SBD từ danh sách import). Thanh toán thực hiện khi sát hạch.
     */
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
        row.setStatusLabel("Được xét duyệt");
    }

    /**
     * Gán badge cho trang my-exams: chờ xét duyệt (chưa có SBD) → được xét duyệt (có SBD, chờ ngày thi).
     */
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
            row.setOverallResultLabel("-");
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
            row.setStatusLabel("Chờ công bố");
            row.setOverallResultLabel("Chờ công bố");
        } else {
            row.setStatusClass("info");
            row.setStatusLabel("Được xét duyệt");
            row.setOverallResultLabel("Chờ đến ngày thi");
        }

        row.setCancelRequested(ExamRegistrationLifecycleStatus.isCancellationPending(regStatus));
        row.setCanRequestCancellation(
                ExamRegistrationLifecycleStatus.canRequestCancellation(regStatus, isSbdPending(candidateNumber)));
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

    /**
     * Công bố giờ ca thi sau khi đã có điểm hoặc ca đã diễn ra - phòng trường hợp thi xong
     * nhưng trạng thái Session chưa đồng bộ.
     */
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

    /** Định dạng khoảng giờ ca thi cho dashboard (vd: "08:00 - 10:00"). */
    public static String formatSessionTimeRange(java.util.Date start, java.util.Date end) {
        if (start == null && end == null) {
            return null;
        }
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm", Locale.ROOT);
        if (start != null && end != null) {
            return hm.format(start) + " - " + hm.format(end);
        }
        return start != null ? hm.format(start) : hm.format(end);
    }

    /** Đọc cột INT/BIT an toàn - tương thích mọi JDBC driver (tránh pattern matching gây lỗi compile NB). */
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

    // --- Quy tắc đăng ký ca thi ---

    public static String validateNewSessionRegistration(ExamRegistrationDAO examRegistrationdao,
            int profileId, int sessionId, int licenceId, String uiLicenceCode) {
        SessionExamSectionInfo section = examRegistrationdao.findPrimarySectionForSession(sessionId);
        if (section == null) {
            return "Không xác định được phần thi của ca thi này. Vui lòng chọn ca khác hoặc liên hệ Ban sát hạch.";
        }

        RegistrantSectionRegistrationBlock block = examRegistrationdao.findActiveSectionRegistration(
                profileId, licenceId, section.getSectionId());
        if (block != null) {
            String statusLabel = ExamRegistrationLifecycleStatus.toDisplayLabel(block.getRegistrationStatus());
            return String.format(
                    "Bạn đã có đăng ký phần thi %s (Hạng %s) tại %s - trạng thái: %s. "
                            + "Chỉ được đăng ký lại khi đăng ký trước bị từ chối hoặc đã được hủy.",
                    section.getSectionName(),
                    uiLicenceCode != null ? uiLicenceCode : "-",
                    block.getSessionName() != null ? block.getSessionName() : "-",
                    statusLabel);
        }

        SessionScheduleInfo newSchedule = examRegistrationdao.findSessionSchedule(sessionId);
        if (newSchedule == null || newSchedule.getExamDate() == null) {
            return null;
        }

        List<SessionScheduleInfo> activeSchedules =
                examRegistrationdao.listActiveSessionSchedulesByProfileId(profileId);
        for (SessionScheduleInfo existing : activeSchedules) {
            if (existing.getLicenceId() == licenceId) {
                continue;
            }
            if (existing.getSessionId() == sessionId) {
                continue;
            }
            String scheduleConflict = validateCrossLicenceScheduleConflict(newSchedule, existing);
            if (scheduleConflict != null) {
                return scheduleConflict;
            }
        }
        return null;
    }

    /** Giữa các hạng GPLX khác nhau: chỉ cho phép thi vào ngày khác nhau. */
    static String validateCrossLicenceScheduleConflict(SessionScheduleInfo candidate,
            SessionScheduleInfo existing) {
        if (candidate.getExamDate() == null || existing.getExamDate() == null) {
            return null;
        }
        if (!sameCalendarDay(candidate.getExamDate(), existing.getExamDate())) {
            return null;
        }

        String dateLabel = formatExamDate(existing.getExamDate());
        String licenceLabel = existing.getUiLicenceCode() != null ? existing.getUiLicenceCode() : "-";
        String sessionLabel = existing.getSessionName() != null ? existing.getSessionName() : "-";
        return String.format(
                "Bạn đã có ca thi Hạng %s (%s) vào ngày %s. "
                        + "Giữa các hạng GPLX khác nhau chỉ được thi vào ngày khác nhau - vui lòng chọn ca khác ngày.",
                licenceLabel, sessionLabel, dateLabel);
    }

    static boolean sameCalendarDay(Date a, Date b) {
        Calendar calA = Calendar.getInstance(Locale.getDefault());
        calA.setTime(a);
        Calendar calB = Calendar.getInstance(Locale.getDefault());
        calB.setTime(b);
        return isSameDay(calA, calB);
    }

    private static String formatExamDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date);
    }
}
