package examstaff.util;

import examstaff.dto.ExamSummaryDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExamStaffSessionRules {

    private ExamStaffSessionRules() {
    }

    public static List<ExamSummaryDTO> sessionsForExam(List<ExamSummaryDTO> allSessions, int examId) {
        List<ExamSummaryDTO> result = new ArrayList<>();
        if (allSessions == null || examId <= 0) {
            return result;
        }
        for (ExamSummaryDTO s : allSessions) {
            if (s != null && (s.getExamId() == examId || s.getId() == examId)) {
                result.add(s);
            }
        }
        return result;
    }

    public static int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId) {
        if (examId > 0) {
            return examId;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            return 0;
        }
        ExamSummaryDTO first = allSessions.get(0);
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    public static ExamSummaryDTO findExamById(List<ExamSummaryDTO> allSessions, int examId) {
        if (allSessions == null || examId <= 0) {
            return null;
        }
        for (ExamSummaryDTO s : allSessions) {
            if (s != null && (s.getId() == examId || s.getExamId() == examId)) {
                return s;
            }
        }
        return null;
    }

    public static int resolveDefaultExamId(List<ExamSummaryDTO> allSessions) {
        if (allSessions == null || allSessions.isEmpty()) {
            return 0;
        }
        ExamSummaryDTO first = allSessions.get(0);
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    public static List<ExamSummaryDTO> sortExamDaysForSidebar(List<ExamSummaryDTO> options) {
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        List<ExamSummaryDTO> sorted = new ArrayList<>(options);
        sorted.sort(Comparator
                .comparing(ExamSummaryDTO::getExamDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(s -> s.getLicenseCode() != null ? s.getLicenseCode() : "",
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ExamSummaryDTO::getId));
        return sorted;
    }
}
