package Utils;

import Constants.ExamRegistrationLifecycleStatus;
import DAO.ExamRegistrationDAO;
import Models.RegistrantSectionRegistrationBlock;
import Models.SessionExamSectionInfo;
import Models.SessionScheduleInfo;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Quy tắc nghiệp vụ đăng ký ca thi: một phần thi / hạng GPLX, trùng ca giữa các hạng.
 */
public final class RegistrantExamRegistrationRules {

    private RegistrantExamRegistrationRules() {
    }

    public static String validateNewSessionRegistration(ExamRegistrationDAO examRegistrationDAO,
            int profileId, int sessionId, int licenceId, String uiLicenceCode) {
        SessionExamSectionInfo section = examRegistrationDAO.findPrimarySectionForSession(sessionId);
        if (section == null) {
            return "Không xác định được phần thi của ca thi này. Vui lòng chọn ca khác hoặc liên hệ Ban sát hạch.";
        }

        RegistrantSectionRegistrationBlock block = examRegistrationDAO.findActiveSectionRegistration(
                profileId, licenceId, section.getSectionId());
        if (block != null) {
            String statusLabel = ExamRegistrationLifecycleStatus.toDisplayLabel(block.getRegistrationStatus());
            return String.format(
                    "Bạn đã có đăng ký phần thi %s (Hạng %s) tại %s — trạng thái: %s. "
                            + "Chỉ được đăng ký lại khi đăng ký trước bị từ chối hoặc đã được hủy.",
                    section.getSectionName(),
                    uiLicenceCode != null ? uiLicenceCode : "—",
                    block.getSessionName() != null ? block.getSessionName() : "—",
                    statusLabel);
        }

        SessionScheduleInfo newSchedule = examRegistrationDAO.findSessionSchedule(sessionId);
        if (newSchedule == null || newSchedule.getExamDate() == null) {
            return null;
        }

        List<SessionScheduleInfo> activeSchedules =
                examRegistrationDAO.listActiveSessionSchedulesByProfileId(profileId);
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

        String dateLabel = formatDate(existing.getExamDate());
        String licenceLabel = existing.getUiLicenceCode() != null ? existing.getUiLicenceCode() : "—";
        String sessionLabel = existing.getSessionName() != null ? existing.getSessionName() : "—";
        return String.format(
                "Bạn đã có ca thi Hạng %s (%s) vào ngày %s. "
                        + "Giữa các hạng GPLX khác nhau chỉ được thi vào ngày khác nhau — vui lòng chọn ca khác ngày.",
                licenceLabel, sessionLabel, dateLabel);
    }

    static boolean sameCalendarDay(Date a, Date b) {
        Calendar calA = Calendar.getInstance(Locale.getDefault());
        calA.setTime(a);
        Calendar calB = Calendar.getInstance(Locale.getDefault());
        calB.setTime(b);
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR)
                && calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR);
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date);
    }
}
