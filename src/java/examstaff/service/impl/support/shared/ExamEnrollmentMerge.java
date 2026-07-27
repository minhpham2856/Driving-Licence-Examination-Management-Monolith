package examstaff.service.impl.support.shared;

import examstaff.dto.ExamRegistrationDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Utility gộp nhiều dòng enrollment cùng thí sinh (candidateId) thành một
 * ExamRegistrationDTO hiển thị — xử lý trùng lặp từ JOIN nhiều ca/phần thi.
 *
 * Vai trò trong luồng examstaff:
 * Query JDBC đôi khi trả nhiều row/enrollment cho cùng thí sinh (nhiều ExamEnrollment, điểm LT/TH riêng).
 * Trước khi bind dashboard, hàng đợi gọi hoặc public snapshot, BLL gọi deduplicateByCandidate
 * để staff chỉ thấy một hàng hợp nhất với cờ/điểm/ảnh/khu vực “đầy đủ” nhất.
 *
 * Cách hoạt động:
 * - deduplicateByCandidate — LinkedHashMap theo id; merge trùng;
 *       sort theo candidateNo.
 * - merge — chọn primary theo rowPriority (thanh toán, ảnh, điểm, phòng, trừ vắng/đình chỉ);
 *       OR cờ trạng thái; gộp điểm với ưu tiên failed > passed > none.
 *
 * Ai gọi:
 * ExamRegistrationDAOImpl, CandidateQueueServiceImpl, StaffCallServiceImpl,
 * PublicCallSnapshotSupport, ExamStaffDashboardServiceImpl — mọi list thí sinh từ DAO view.
 */
public final class ExamEnrollmentMerge {

    private ExamEnrollmentMerge() {
    }

    /**
     * Gộp theo candidate id, giữ giá trị “đầy đủ” hơn; sắp theo candidateNo.
     * @param raw danh sách gốc (có thể trùng id)
     * @return danh sách đã dedupe (không null)
     */
    public static List<ExamRegistrationDTO> deduplicateByCandidate(List<ExamRegistrationDTO> raw) {
        // Validate
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        // Mutate: gộp theo candidate id, giữ bản “đầy đủ” hơn
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
        // Result: sắp theo candidateNo
        List<ExamRegistrationDTO> result = new ArrayList<>(byCandidate.values());
        result.sort(Comparator.comparingInt(ExamRegistrationDTO::getCandidateNo));
        return result;
    }

    /**
     * Gộp hai dòng cùng thí sinh vào bản primary (mutate primary).
     * @param a dòng thứ nhất
     * @param b dòng thứ hai
     * @return bản ghi primary sau khi gộp cờ/điểm/ảnh/khu vực
     */
    static ExamRegistrationDTO merge(ExamRegistrationDTO a, ExamRegistrationDTO b) {
        // Load: chọn primary theo độ đầy đủ
        ExamRegistrationDTO primary = preferPrimaryRow(a, b);
        ExamRegistrationDTO secondary = primary == a ? b : a;

        // Mutate: OR các cờ trạng thái; bổ sung ảnh/điểm/khu vực thiếu
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
        primary.setWrongCriticalTheory(
                primary.hasWrongCriticalTheory() || secondary.hasWrongCriticalTheory());
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

        // Result
        return primary;
    }

    /**
     * Chọn dòng ưu tiên theo điểm độ đầy đủ / trạng thái.
     * @param a dòng A
     * @param b dòng B
     * @return dòng có priority cao hơn (hòa → examId nhỏ hơn)
     */
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

    /**
     * Điểm ưu tiên để chọn bản ghi “tốt” hơn khi merge.
     * @param c hồ sơ đăng ký
     * @return điểm cộng/trừ theo thanh toán, ảnh, điểm, khu vực, vắng/đình chỉ
     */
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

    /**
     * Gộp điểm + cờ đạt của một phần (LT hoặc TH).
     * @param primary   bản ghi đích (mutate)
     * @param secondary bản ghi nguồn
     * @param theory    true = lý thuyết, false = thực hành
     */
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

    /**
     * Ưu tiên failed > passed > none khi gộp cờ đạt.
     * @param a trạng thái A
     * @param b trạng thái B
     * @return trạng thái đã gộp
     */
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

    /**
     * null/blank → none, còn lại lower-case.
     * @param v chuỗi gốc
     * @return none hoặc chuỗi đã trim/lower
     */
    private static String nullToNone(String v) {
        return v == null || v.isBlank() ? "none" : v.trim().toLowerCase(Locale.ROOT);
    }
}
