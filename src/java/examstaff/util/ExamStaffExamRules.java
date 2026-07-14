package examstaff.util;

import examstaff.dto.ExamSummaryDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lọc / chọn / sắp xếp danh sách kỳ thi cho exam staff. */
public final class ExamStaffExamRules {

    private ExamStaffExamRules() {
    }

    /**
     * Lấy các bản ghi cùng {@code examId} (khớp id hoặc examId).
     *
     * @param allExams danh sách nguồn
     * @param examId   mã kỳ cần lọc
     * @return danh sách khớp (có thể rỗng)
     */
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

    /**
     * Chọn examId chính: giữ {@code examId} nếu &gt; 0, không thì id phần tử đầu.
     *
     * @param allExams danh sách kỳ
     * @param examId   id gợi ý
     * @return examId hợp lệ hoặc {@code 0}
     */
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

    /**
     * Tìm một kỳ theo id (khớp {@code id} hoặc {@code examId}).
     *
     * @param allExams danh sách
     * @param examId   mã cần tìm
     * @return DTO hoặc {@code null}
     */
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

    /**
     * ExamId mặc định = phần tử đầu danh sách.
     *
     * @param allExams danh sách kỳ
     * @return id hoặc {@code 0}
     */
    public static int resolveDefaultExamId(List<ExamSummaryDTO> allExams) {
        if (allExams == null || allExams.isEmpty()) {
            return 0;
        }
        ExamSummaryDTO first = allExams.get(0);
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    /**
     * Sắp xếp ngày thi cho sidebar: ngày mới trước, rồi hạng, rồi id.
     *
     * @param options danh sách gốc
     * @return bản copy đã sắp (không null)
     */
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
