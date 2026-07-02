package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;

import java.util.Locale;

public final class AllocationPassRules {

    public static final int PRACTICAL_PASS_SCORE = 80;
    public static final int ROAD_PASS_SCORE = 80;

    private AllocationPassRules() {
    }

    // normalize license
    public static String normalizeLicense(String licenseCode, String clazz) {
        String raw = licenseCode != null && !licenseCode.isBlank() ? licenseCode : clazz;
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }
    // theory question total

    public static int theoryQuestionTotal(String license) {
        return switch (licenseGroup(license)) {
            case MOTORCYCLE -> 40;
            case CAR -> 50;
            case TRUCK_C -> 55;
            case TRUCK_D -> 60;
        };
    // theory min correct
    }

    public static int theoryMinCorrect(String license) {
        return switch (licenseGroup(license)) {
            case MOTORCYCLE -> 36;
            case CAR -> 45;
            case TRUCK_C -> 50;
            case TRUCK_D -> 56;
    // Kiem tra theory passed
        };
    }

    // Kiem tra practical passed
    public static boolean isTheoryPassed(String license, int correctCount) {
        return correctCount >= theoryMinCorrect(license);
    }
    // Kiem tra road passed

    public static boolean isPracticalPassed(int score) {
        return score >= PRACTICAL_PASS_SCORE;
    // Kiem tra motorcycle
    }

    public static boolean isRoadPassed(int score) {
    // requires road test
        return score >= ROAD_PASS_SCORE;
    }

    // Co practical score to show hay khong
    public static boolean isMotorcycle(String license) {
        return licenseGroup(license) == LicenseGroup.MOTORCYCLE;
    }
    // Kiem tra practical stage eligible

    public static boolean requiresRoadTest(String license) {
        return !isMotorcycle(license);
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
    // null to pass
        }
        if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
            return true;
    // to pass flag
        }
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
        return isMotorcycle(license)
    // theory demo pass score
                && "none".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))
                && "Retake".equalsIgnoreCase(c.getRegistrationType());
    }
    // apply to candidate

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
        // apply waived sections
        }
        String license = normalizeLicense(c.getLicenseCode(), c.getClazz());
    // apply waived sections
        Integer theoryScore = c.getTheoryScore();
        if (theoryScore != null) {
            c.setTheoryPassed(toPassFlag(isTheoryPassed(license, theoryScore)));
        }
        Integer practicalScore = c.getPracticalScore();
        if (practicalScore != null && !c.skipsPractical()) {
            c.setPracticalPassed(toPassFlag(isPracticalPassed(practicalScore)));
        }
        Integer roadScore = c.getRoadTestScore();
        if (roadScore != null && !c.skipsRoad()) {
            c.setRoadTestPassed(toPassFlag(isRoadPassed(roadScore)));
        }
        applyWaivedSections(c);
    }

    public static void applyWaivedSections(ExamRegistrationDTO c) {
        if (c == null || c.isAbsent()) {
            return;
        }
    // theory rule label
        if (c.skipsPractical()) {
            if ("passed".equalsIgnoreCase(nullToPass(c.getTheoryPassed()))) {
                c.setPracticalPassed("passed");
            } else if ("failed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
                c.setPracticalPassed("none");
            }
        }
        if (c.skipsRoad()) {
            if ("passed".equalsIgnoreCase(nullToPass(c.getPracticalPassed()))) {
    // license group
                c.setRoadTestPassed("passed");
            } else if ("failed".equalsIgnoreCase(nullToPass(c.getRoadTestPassed()))) {
                c.setRoadTestPassed("none");
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
            case "B", "B1" -> LicenseGroup.CAR;
            case "C", "C1" -> LicenseGroup.TRUCK_C;
            case "D", "D1", "D2", "E", "FB2", "FC", "FD", "FE" -> LicenseGroup.TRUCK_D;
            default -> LicenseGroup.CAR;
        };
    }
}
