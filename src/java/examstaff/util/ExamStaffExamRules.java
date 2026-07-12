package examstaff.util;

import examstaff.dto.ExamSummaryDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExamStaffExamRules {

    private ExamStaffExamRules() {
    }

    public static List<ExamSummaryDTO> examsForExam(List<ExamSummaryDTO> allExams, int examId) {
        List<ExamSummaryDTO> result = new ArrayList<>();
        if (allExams == null || examId <= 0) {
            return result;
        }
        for (ExamSummaryDTO s : allExams) {
            if (s != null && (s.getExamId() == examId || s.getId() == examId)) {
                result.add(s);
            }
        }
        return result;
    }

    public static int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId) {
        if (examId > 0) {
            return examId;
        }
        if (allExams == null || allExams.isEmpty()) {
            return 0;
        }
        ExamSummaryDTO first = allExams.get(0);
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    public static ExamSummaryDTO findExamById(List<ExamSummaryDTO> allExams, int examId) {
        if (allExams == null || examId <= 0) {
            return null;
        }
        for (ExamSummaryDTO s : allExams) {
            if (s != null && (s.getId() == examId || s.getExamId() == examId)) {
                return s;
            }
        }
        return null;
    }

    public static int resolveDefaultExamId(List<ExamSummaryDTO> allExams) {
        if (allExams == null || allExams.isEmpty()) {
            return 0;
        }
        ExamSummaryDTO first = allExams.get(0);
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
