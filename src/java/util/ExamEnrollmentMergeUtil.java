package util;

import dto.exam.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class ExamEnrollmentMergeUtil {

    private ExamEnrollmentMergeUtil() {
    }

    // deduplicate by candidate
    public static List<ExamRegistrationDTO> deduplicateByCandidate(List<ExamRegistrationDTO> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<Integer, ExamRegistrationDTO> byCandidate = new LinkedHashMap<>();
        for (ExamRegistrationDTO row : raw) {
            if (row == null || row.getId() <= 0) {
                continue;
            }
            ExamRegistrationDTO existing = byCandidate.get(row.getId());
            if (existing == null) {
                byCandidate.put(row.getId(), row);
            } else {
                byCandidate.put(row.getId(), merge(existing, row));
            }
        }
        List<ExamRegistrationDTO> result = new ArrayList<>(byCandidate.values());
        result.sort(Comparator.comparingInt(ExamRegistrationDTO::getCandidateNo));
        return result;
    }

    static ExamRegistrationDTO merge(ExamRegistrationDTO a, ExamRegistrationDTO b) {
        ExamRegistrationDTO primary = preferPrimaryRow(a, b);
        ExamRegistrationDTO secondary = primary == a ? b : a;

        primary.setAbsent(a.isAbsent() || b.isAbsent());
        primary.setSuspended(a.isSuspended() || b.isSuspended());
        primary.setIsPaymentCompleted(a.isPaymentCompleted() || b.isPaymentCompleted());
        primary.setIsPresent(a.isPresent() || b.isPresent());

        if (!primary.isValidCapturedPhoto() && secondary.isValidCapturedPhoto()) {
            primary.setValidCapturedPhoto(true);
        }
        if ((primary.getPhotoUrl() == null || primary.getPhotoUrl().isBlank())
                && secondary.getPhotoUrl() != null && !secondary.getPhotoUrl().isBlank()) {
            primary.setPhotoUrl(secondary.getPhotoUrl());
        }

        if (primary.getExamEnrollmentId() <= 0 && secondary.getExamEnrollmentId() > 0) {
            primary.setExamEnrollmentId(secondary.getExamEnrollmentId());
        }
        if (primary.getExamSessionId() <= 0 && secondary.getExamSessionId() > 0) {
            primary.setExamSessionId(secondary.getExamSessionId());
        }
        // merge score field
        // merge score field
        // merge road score

        mergeScoreField(primary, secondary, true);
        mergeScoreField(primary, secondary, false);
        mergeRoadScore(primary, secondary);

        Integer primaryAreaId = primary.getAllocatedAreaId();
        Integer secondaryAreaId = secondary.getAllocatedAreaId();
        boolean differentSessions = primary.getExamSessionId() > 0 && secondary.getExamSessionId() > 0
                && primary.getExamSessionId() != secondary.getExamSessionId();
        if (!differentSessions
                && (primaryAreaId == null || primaryAreaId <= 0)
                && secondaryAreaId != null && secondaryAreaId > 0) {
            primary.setAllocatedAreaId(secondaryAreaId);
            primary.setAllocatedAreaName(secondary.getAllocatedAreaName());
        }
        if ((primary.getComputerCode() == null || primary.getComputerCode().isBlank())
                && secondary.getComputerCode() != null && !secondary.getComputerCode().isBlank()) {
            primary.setComputerCode(secondary.getComputerCode());
        }
    // prefer primary row

        return primary;
    }

    private static ExamRegistrationDTO preferPrimaryRow(ExamRegistrationDTO a, ExamRegistrationDTO b) {
        int scoreA = rowPriority(a);
        int scoreB = rowPriority(b);
        if (scoreB > scoreA) {
            return b;
        }
        if (scoreA > scoreB) {
    // row priority
            return a;
        }
        return a.getExamSessionId() <= b.getExamSessionId() ? a : b;
    }

    private static int rowPriority(ExamRegistrationDTO c) {
        int score = 0;
        if (c.isPaymentCompleted()) {
            score += 4;
        }
        if (c.isValidCapturedPhoto() || (c.getPhotoUrl() != null && !c.getPhotoUrl().isBlank())) {
            score += 4;
        }
        if (!"none".equalsIgnoreCase(nullToNone(c.getTheoryPassed()))) {
            score += 2;
        }
        if (!"none".equalsIgnoreCase(nullToNone(c.getPracticalPassed()))) {
            score += 2;
        }
        if (c.getRoadTestPassed() != null && !c.getRoadTestPassed().isBlank()
                && !"none".equalsIgnoreCase(c.getRoadTestPassed())) {
            score += 1;
        }
        if (c.isAbsent()) {
            score -= 10;
        }
        if (c.getAllocatedAreaId() != null && c.getAllocatedAreaId() > 0) {
            score += 8;
        }
    // merge score field
        if (c.isSuspended()) {
            score -= 10;
        }
        return score;
    }

    private static void mergeScoreField(ExamRegistrationDTO primary, ExamRegistrationDTO secondary, boolean theory) {
        String p = theory ? primary.getTheoryPassed() : primary.getPracticalPassed();
        String s = theory ? secondary.getTheoryPassed() : secondary.getPracticalPassed();
        String merged = mergePassStatus(p, s);
        if (theory) {
            if (primary.getTheoryScore() == null && secondary.getTheoryScore() != null) {
                primary.setTheoryScore(secondary.getTheoryScore());
            }
            primary.setTheoryPassed(merged);
        } else {
    // merge road score
            if (primary.getPracticalScore() == null && secondary.getPracticalScore() != null) {
                primary.setPracticalScore(secondary.getPracticalScore());
            }
            primary.setPracticalPassed(merged);
        }
    }

    // merge pass status
    private static void mergeRoadScore(ExamRegistrationDTO primary, ExamRegistrationDTO secondary) {
        String merged = mergePassStatus(primary.getRoadTestPassed(), secondary.getRoadTestPassed());
        if (primary.getRoadTestScore() == null && secondary.getRoadTestScore() != null) {
            primary.setRoadTestScore(secondary.getRoadTestScore());
        }
        primary.setRoadTestPassed(merged);
    }

    private static String mergePassStatus(String a, String b) {
        String sa = nullToNone(a);
        String sb = nullToNone(b);
    // null to none
        if ("failed".equalsIgnoreCase(sa) || "failed".equalsIgnoreCase(sb)) {
            return "failed";
        }
        if ("passed".equalsIgnoreCase(sa) || "passed".equalsIgnoreCase(sb)) {
            return "passed";
        }
        return "none";
    }

    private static String nullToNone(String v) {
        return v == null || v.isBlank() ? "none" : v.trim().toLowerCase(Locale.ROOT);
    }
}
