package examiner.service.impl;

import examiner.dao.ExamEnrollmentSectionDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.impl.ExamEnrollmentSectionDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import shared.enums.CandidateStatus;
import shared.enums.SectionType;
import shared.util.SectionStatusUtil;
import java.util.List;
import java.util.Map;
import examiner.service.ProgressService;
import shared.model.ExamResult;

// Updates and reads per-section candidate status and result-print flags on enrollments.
public class ProgressServiceImpl implements ProgressService {

    private final ExamEnrollmentSectionDAO enrollmentSectionDAO = new ExamEnrollmentSectionDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();

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

    // Returns whether practical entry is allowed given theory/layout flags and theory completion/pass result.
    @Override
    public boolean isPracticalEntryAllowed(int examEnrollmentId, boolean takeTheory, boolean takeLayout) {
        if (!takeLayout) {
            return false;
        }
        if (!takeTheory) {
            return true;
        }
        CandidateStatus theoryStatus = get(examEnrollmentId, SectionType.THEORY);
        if (theoryStatus != CandidateStatus.COMPLETED) {
            return false;
        }
        ExamResult result = examResultDAO.getByExamEnrollmentId(examEnrollmentId);
        return result != null && result.isPassed();
    }
}
