package examstaff.service.impl.support.allocation;

import examstaff.service.impl.support.shared.LicenseClassRules;
import examstaff.dto.ExamRegistrationDTO;

/**
 * Quy tắc điểm đạt / điều kiện vào giai đoạn thực hành theo hạng GPLX.
 * <p>
 * Lý thuyết (A, A1, B1): đạt khi đúng ≥ {@link #THEORY_PASS_CORRECT}/{@link #THEORY_MAX_QUESTIONS}
 * câu và <strong>không sai câu điểm liệt</strong> ({@code Question.IsCritical}).
 */
public final class AllocationPassRules {

    /** Tổng số câu LT theo quy định hiện hành (A/A1/B1). */
    public static final int THEORY_MAX_QUESTIONS = 25;

    /** Số câu đúng tối thiểu để đạt LT (A/A1/B1). */
    public static final int THEORY_PASS_CORRECT = 21;

    public static final int PRACTICAL_PASS_SCORE = 80;

    private AllocationPassRules() {
    }

    /**
     * Chuẩn hóa hạng từ licenseCode hoặc clazz (fallback).
     *
     * @param licenseCode mã hạng ưu tiên
     * @param clazz       hạng dự phòng
     * @return mã đã normalize theo {@link LicenseClassRules}
     */
    public static String normalizeLicense(String licenseCode, String clazz) {
        String raw = licenseCode != null && !licenseCode.isBlank() ? licenseCode : clazz;
        return LicenseClassRules.normalizeManaged(raw);
    }

    /**
     * Số câu đúng tối thiểu để đạt lý thuyết (A/A1/B1: {@link #THEORY_PASS_CORRECT}).
     *
     * @param license mã hạng (sau normalize hoặc thô) — hiện mọi hạng quản lý dùng cùng ngưỡng
     * @return ngưỡng câu đúng
     */
    public static int theoryMinCorrect(String license) {
        return THEORY_PASS_CORRECT;
    }

    /**
     * Tổng câu đề LT theo hạng (A/A1/B1: {@link #THEORY_MAX_QUESTIONS}).
     *
     * @param license mã hạng
     * @return tổng câu
     */
    public static int theoryMaxQuestions(String license) {
        return THEORY_MAX_QUESTIONS;
    }

    /**
     * Đạt lý thuyết theo số câu đúng (chưa xét câu liệt).
     * Chỉ dùng khi không có dữ liệu điểm liệt; ưu tiên overload có {@code hasWrongCritical}.
     *
     * @param license      mã hạng
     * @param correctCount số câu đúng
     * @return {@code true} nếu đủ số câu đúng
     */
    public static boolean isTheoryPassed(String license, int correctCount) {
        return isTheoryPassed(license, correctCount, false);
    }

    /**
     * Đạt lý thuyết: đủ số câu đúng và không sai câu điểm liệt.
     *
     * @param license           mã hạng
     * @param correctCount      số câu đúng
     * @param hasWrongCritical  {@code true} nếu đã trả lời sai ít nhất một câu {@code IsCritical}
     * @return {@code true} nếu đạt
     */
    public static boolean isTheoryPassed(String license, int correctCount, boolean hasWrongCritical) {
        if (hasWrongCritical) {
            return false;
        }
        return correctCount >= theoryMinCorrect(license);
    }

    /**
     * Đạt thực hành nếu điểm ≥ {@link #PRACTICAL_PASS_SCORE}.
     *
     * @param score điểm thực hành
     * @return {@code true} nếu đạt
     */
    public static boolean isPracticalPassed(int score) {
        return score >= PRACTICAL_PASS_SCORE;
    }

    /**
     * Hạng mô tô (nhóm A/A1).
     *
     * @param license mã hạng
     * @return {@code true} nếu A/A1
     */
    public static boolean isMotorcycle(String license) {
        return LicenseClassRules.isMotorcycle(license);
    }

    /**
     * Thí sinh đủ điều kiện vào giai đoạn thực hành / sa hình.
     * Bảo lưu lý thuyết ({@code TakeTheory = 0}) → vào TH ngay sau thủ tục.
     *
     * @param c hồ sơ đăng ký
     * @return {@code true} nếu đủ điều kiện
     */
    public static boolean isPracticalStageEligible(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || !c.isProcedureComplete() || c.skipsPractical()) {
            return false;
        }
        if ("passed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
            return false;
        }
        // Bảo lưu LT: bỏ qua phòng LT, chỉ thi thực hành
        if (c.skipsTheory()) {
            return true;
        }
        if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
            return true;
        }
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        return isMotorcycle(license)
                && "none".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))
                && "Retake".equalsIgnoreCase(c.getRegistrationType());
    }

    private static String nullToPass(String v) {
        return v == null || v.isBlank() ? "none" : v.trim();
    }

    /**
     * Boolean đạt → cờ {@code passed}/{@code failed}.
     *
     * @param passed kết quả
     * @return chuỗi cờ
     */
    public static String toPassFlag(boolean passed) {
        return passed ? "passed" : "failed";
    }

    /**
     * Ghi cờ đạt LT/TH lên DTO từ điểm hiện có (bỏ qua nếu vắng).
     * LT: chỉ dựa số câu đúng — nếu đã biết sai câu liệt, gọi
     * {@link #applyTheoryResult(ExamRegistrationDTO, boolean)} trước/sau.
     *
     * @param c hồ sơ (mutate)
     */
    public static void applyToCandidate(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent()) {
            return;
        }
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        Integer theoryScore = c.getTheoryScore();
        if (theoryScore != null && !c.skipsTheory()) {
            c.setTheoryPassed(toPassFlag(isTheoryPassed(license, theoryScore)));
        }
        Integer practicalScore = c.getPracticalScore();
        if (practicalScore != null && !c.skipsPractical()) {
            c.setPracticalPassed(toPassFlag(isPracticalPassed(practicalScore)));
        }
        applyWaivedSections(c);
    }

    /**
     * Áp kết quả LT khi đã biết có/không sai câu liệt.
     *
     * @param c                hồ sơ (mutate)
     * @param hasWrongCritical đã sai câu điểm liệt
     */
    public static void applyTheoryResult(ExamRegistrationDTO c, boolean hasWrongCritical) {
        if (c == null || c.isAbsent() || c.skipsTheory()) {
            return;
        }
        Integer theoryScore = c.getTheoryScore();
        if (theoryScore == null) {
            return;
        }
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        c.setTheoryPassed(toPassFlag(isTheoryPassed(license, theoryScore, hasWrongCritical)));
        applyWaivedSections(c);
    }

    /**
     * Xử lý phần được miễn (skip practical → đồng bộ cờ theo LT).
     *
     * @param c hồ sơ (mutate)
     */
    public static void applyWaivedSections(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent()) {
            return;
        }
        if (c.skipsPractical()) {
            if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
                c.setPracticalPassed("passed");
            } else if ("failed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
                c.setPracticalPassed("none");
            }
        }
    }
}
