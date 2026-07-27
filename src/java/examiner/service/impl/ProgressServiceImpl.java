package examiner.service.impl;

import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import shared.enums.CandidateStatus;
import shared.enums.SectionType;
import shared.util.SectionStatusUtil;
import java.util.List;
import java.util.Map;
import examiner.service.ProgressService;

// Updates and reads per-section candidate status and result-print flags on enrollments.
public class ProgressServiceImpl implements ProgressService {

    private static final int THEORY_PASS_CORRECT = 21;

    private final ExamEnrollmentSectionDAO enrollmentSectionDAO = new ExamEnrollmentSectionDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();

    // Reads normalized candidate status for one enrollment section row.
    @Override
    public CandidateStatus get(int examEnrollmentId, SectionType sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null) {
            return CandidateStatus.NOT_STARTED;
        }
        Map<Integer, String> statuses = enrollmentSectionDAO.getStatusByEnrollmentIds(
                List.of(examEnrollmentId), sectionType.getValue());
        String raw = statuses.get(examEnrollmentId);
        String normalized = SectionStatusUtil.normalize(raw);
        CandidateStatus status = CandidateStatus.fromValue(normalized);
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }

    // Updates candidate status on the enrollment section row.
    @Override
    public boolean update(int examEnrollmentId, SectionType sectionType, CandidateStatus status) {
        if (examEnrollmentId <= 0 || sectionType == null || status == null) {
            return false;
        }
        return enrollmentSectionDAO.updateStatusByEnrollmentIdAndSectionType(
                examEnrollmentId, sectionType.getValue(), status.getValue());
    }

    // Ensures an ExamEnrollmentSection row exists for the enrollment and section.
    @Override
    public boolean add(int examEnrollmentId, SectionType sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null) {
            return false;
        }
        return enrollmentSectionDAO.ensureSectionRow(examEnrollmentId, sectionType.getValue());
    }

    // Returns whether the result form was marked printed for this section.
    @Override
    public boolean isResultPrinted(int examEnrollmentId, SectionType sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null) {
            return false;
        }
        return enrollmentSectionDAO.isResultPrinted(examEnrollmentId, sectionType.getValue());
    }

    // Sets the result-printed flag for this enrollment section.
    @Override
    public boolean markResultPrinted(int examEnrollmentId, SectionType sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null) {
            return false;
        }
        return enrollmentSectionDAO.markResultPrinted(examEnrollmentId, sectionType.getValue());
    }

    // Practical entry: theory chờ ký/hoàn thành + điểm LT >= 21 (không dùng ExamResult.IsPassed tổng).
    @Override
    public boolean isPracticalEntryAllowed(int examEnrollmentId, boolean takeTheory, boolean takeLayout) {
        if (!takeLayout) {
            return false;
        }
        if (!takeTheory) {
            return true;
        }
        CandidateStatus theoryStatus = get(examEnrollmentId, SectionType.THEORY);
        if (theoryStatus != CandidateStatus.COMPLETED
                && theoryStatus != CandidateStatus.AWAITING_SIGNATURE) {
            return false;
        }
        Double theoryScore = examScoreDAO.getScoreByEnrollmentAndSectionType(
                examEnrollmentId, SectionType.THEORY.getValue());
        return theoryScore != null && theoryScore >= THEORY_PASS_CORRECT;
    }
}
