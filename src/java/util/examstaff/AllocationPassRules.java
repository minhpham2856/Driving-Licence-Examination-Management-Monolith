package util.examstaff;

import dto.exam.ExamRegistrationDTO;

public final class AllocationPassRules {

    public static final int PRACTICAL_PASS_SCORE = 80;

    private AllocationPassRules() {
    }

    public static String normalizeLicense(String licenseCode, String clazz) {
        String raw = licenseCode != null && !licenseCode.isBlank() ? licenseCode : clazz;
        return LicenseClassRules.normalizeManaged(raw);
    }

    public static int theoryQuestionTotal(String license) {
        return switch (licenseGroup(license)) {
            case MOTORCYCLE -> 40;
            case CAR -> 50;
            case TRUCK_C -> 55;
            case TRUCK_D -> 60;
        };
    }

    public static int theoryMinCorrect(String license) {
        return switch (licenseGroup(license)) {
            case MOTORCYCLE -> 36;
            case CAR -> 45;
            case TRUCK_C -> 50;
            case TRUCK_D -> 56;
        };
    }

    public static boolean isTheoryPassed(String license, int correctCount) {
        return correctCount >= theoryMinCorrect(license);
    }

    public static boolean isPracticalPassed(int score) {
        return score >= PRACTICAL_PASS_SCORE;
    }

    public static boolean isMotorcycle(String license) {
        return licenseGroup(license) == LicenseGroup.MOTORCYCLE;
    }

    public static boolean hasPracticalScoreToShow(ExamRegistrationDTO c) {
        return c != null && c.getPracticalScore() != null;
    }

    public static boolean isPracticalStageEligible(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent() || !c.isProcedureComplete() || c.skipsPractical()) {
            return false;
        }
        if ("passed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
            return false;
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

    public static String toPassFlag(boolean passed) {
        return passed ? "passed" : "failed";
    }

    public static int theoryDemoPassScore(String license) {
        return theoryMinCorrect(license);
    }

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

    public static String theoryRuleLabel(String license) {
        int min = theoryMinCorrect(license);
        int total = theoryQuestionTotal(license);
        return min + "/" + total + " câu đúng, không sai điểm liệt";
    }

    private enum LicenseGroup {
        MOTORCYCLE, CAR, TRUCK_C, TRUCK_D
    }

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
