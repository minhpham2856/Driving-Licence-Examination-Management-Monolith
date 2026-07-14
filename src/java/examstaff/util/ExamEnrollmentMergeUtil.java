package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/** Gộp nhiều dòng enrollment cùng thí sinh thành một bản ghi hiển thị. */
public final class ExamEnrollmentMergeUtil {

    private ExamEnrollmentMergeUtil() {
    }

    /**
     * Gộp theo candidate id, giữ giá trị “đầy đủ” hơn; sắp theo candidateNo.
     *
     * @param raw danh sách gốc (có thể trùng id)
     * @return danh sách đã dedupe (không null)
     */
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

    /** Gộp hai dòng cùng thí sinh vào bản primary (mutate primary). */
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
        if (primary.getExamId() <= 0 && secondary.getExamId() > 0) {
            primary.setExamId(secondary.getExamId());
        }
        mergeScoreField(primary, secondary, true);
        mergeScoreField(primary, secondary, false);

        Integer primaryAreaId = primary.getAllocatedAreaId();
        Integer secondaryAreaId = secondary.getAllocatedAreaId();
        boolean differentExams = primary.getExamId() > 0 && secondary.getExamId() > 0
                && primary.getExamId() != secondary.getExamId();
        if (!differentExams
                && (primaryAreaId == null || primaryAreaId <= 0)
                && secondaryAreaId != null && secondaryAreaId > 0) {
            primary.setAllocatedAreaId(secondaryAreaId);
            primary.setAllocatedAreaName(secondary.getAllocatedAreaName());
        }
        if ((primary.getComputerCode() == null || primary.getComputerCode().isBlank())
                && secondary.getComputerCode() != null && !secondary.getComputerCode().isBlank()) {
            primary.setComputerCode(secondary.getComputerCode());
        }

        return primary;
    }

    /** Chọn dòng ưu tiên theo điểm độ đầy đủ / trạng thái. */
    private static ExamRegistrationDTO preferPrimaryRow(ExamRegistrationDTO a, ExamRegistrationDTO b) {
        int scoreA = rowPriority(a);
        int scoreB = rowPriority(b);
        if (scoreB > scoreA) {
            return b;
        }
        if (scoreA > scoreB) {
            return a;
        }
        return a.getExamId() <= b.getExamId() ? a : b;
    }

    /** Điểm ưu tiên để chọn bản ghi “tốt” hơn khi merge. */
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
        if (c.isAbsent()) {
            score -= 10;
        }
        if (c.getAllocatedAreaId() != null && c.getAllocatedAreaId() > 0) {
            score += 8;
        }
        if (c.isSuspended()) {
            score -= 10;
        }
        return score;
    }

    /** Gộp điểm + cờ đạt của một phần (LT hoặc TH). */
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
            if (primary.getPracticalScore() == null && secondary.getPracticalScore() != null) {
                primary.setPracticalScore(secondary.getPracticalScore());
            }
            primary.setPracticalPassed(merged);
        }
    }

    /** Ưu tiên failed &gt; passed &gt; none khi gộp cờ đạt. */
    private static String mergePassStatus(String a, String b) {
        String sa = nullToNone(a);
        String sb = nullToNone(b);
        if ("failed".equalsIgnoreCase(sa) || "failed".equalsIgnoreCase(sb)) {
            return "failed";
        }
        if ("passed".equalsIgnoreCase(sa) || "passed".equalsIgnoreCase(sb)) {
            return "passed";
        }
        return "none";
    }

    /** null/blank → {@code none}, còn lại lower-case. */
    private static String nullToNone(String v) {
        return v == null || v.isBlank() ? "none" : v.trim().toLowerCase(Locale.ROOT);
    }
}
