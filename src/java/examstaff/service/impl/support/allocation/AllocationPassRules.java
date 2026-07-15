package examstaff.service.impl.support.allocation;
import examstaff.service.impl.support.shared.LicenseClassRules;

import examstaff.dto.ExamRegistrationDTO;

/** Quy tắc điểm đạt / điều kiện vào giai đoạn thực hành theo hạng GPLX. */
public final class AllocationPassRules {

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
     * Số câu đúng tối thiểu để đạt lý thuyết theo nhóm hạng.
     *
     * @param license mã hạng (sau normalize hoặc thô)
     * @return ngưỡng câu đúng
     */
    public static int theoryMinCorrect(String license) {
        return switch (licenseGroup(license)) {
            case MOTORCYCLE -> 36;
            case CAR -> 45;
            case TRUCK_C -> 50;
            case TRUCK_D -> 56;
        };
    }

    /**
     * Đạt lý thuyết nếu số câu đúng ≥ ngưỡng hạng.
     *
     * @param license      mã hạng
     * @param correctCount số câu đúng
     * @return {@code true} nếu đạt
     */
    public static boolean isTheoryPassed(String license, int correctCount) {
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
     * Hạng mô tô (nhóm MOTORCYCLE).
     *
     * @param license mã hạng
     * @return {@code true} nếu A/A1
     */
    public static boolean isMotorcycle(String license) {
        return licenseGroup(license) == LicenseGroup.MOTORCYCLE;
    }

    /**
     * Thí sinh đủ điều kiện vào giai đoạn thực hành / sa hình.
     *
     * @param c hồ sơ đăng ký
     * @return {@code true} nếu đủ điều kiện
     */
    public static boolean isPracticalStageEligible(ExamRegistrationDTO c) {
        // validate: thiếu hồ sơ / vắng / chưa thủ tục / miễn TH → không vào stage
        if (c == null || c.isAbsent() || !c.isProcedureComplete() || c.skipsPractical()) {
            return false;
        }
        // validate: đã đạt TH → không còn trong stage thực hành
        if ("passed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
            return false;
        }
        // result: đã đạt LT → đủ điều kiện TH
        if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
            return true;
        }
        // load + result: mô tô thi lại (Retake) chưa thi LT vẫn vào TH
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        return isMotorcycle(license)
                && "none".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))
                && "Retake".equalsIgnoreCase(c.getRegistrationType());
    }

    /**
     * Chuẩn hóa cờ đạt: null/blank → {@code none}.
     *
     * @param v giá trị thô
     * @return chuỗi đã trim hoặc {@code none}
     */
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
     *
     * @param c hồ sơ (mutate)
     */
    public static void applyToCandidate(ExamRegistrationDTO c) {
        // validate
        if (c == null || c.isAbsent()) {
            return;
        }
        // load hạng GPLX đã chuẩn hóa
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        // mutate: cờ LT từ điểm (nếu không miễn LT)
        Integer theoryScore = c.getTheoryScore();
        if (theoryScore != null && !c.skipsTheory()) {
            c.setTheoryPassed(toPassFlag(isTheoryPassed(license, theoryScore)));
        }
        // mutate: cờ TH từ điểm (nếu không miễn TH)
        Integer practicalScore = c.getPracticalScore();
        if (practicalScore != null && !c.skipsPractical()) {
            c.setPracticalPassed(toPassFlag(isPracticalPassed(practicalScore)));
        }
        // result: đồng bộ phần miễn
        applyWaivedSections(c);
    }

    /**
     * Xử lý phần được miễn (skip practical → đồng bộ cờ theo LT).
     *
     * @param c hồ sơ (mutate)
     */
    public static void applyWaivedSections(ExamRegistrationDTO c) {
        // validate
        if (c == null || c.isAbsent()) {
            return;
        }
        // mutate: miễn TH → đạt LT thì gán TH=passed; TH=failed thì reset none
        if (c.skipsPractical()) {
            if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
                c.setPracticalPassed("passed");
            } else if ("failed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
                c.setPracticalPassed("none");
            }
        }
    }

    private enum LicenseGroup {
        MOTORCYCLE, CAR, TRUCK_C, TRUCK_D
    }

    /** Gom hạng GPLX vào nhóm ngưỡng điểm. */
    private static LicenseGroup licenseGroup(String license) {
        if (license == null || license.isBlank()) {
            return LicenseGroup.CAR;
        }
        return switch (license) {
            case "A", "A1" -> LicenseGroup.MOTORCYCLE;
            case "B1" -> LicenseGroup.CAR;
            case "C", "C1" -> LicenseGroup.TRUCK_C;
            case "D", "D1", "D2", "E", "FB2", "FC", "FD", "FE" -> LicenseGroup.TRUCK_D;
            default -> LicenseGroup.CAR;
        };
    }
}
