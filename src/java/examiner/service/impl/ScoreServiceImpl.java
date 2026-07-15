package examiner.service.impl;

import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import shared.enums.SectionType;
import shared.model.ExamEnrollment;
import shared.model.ExamResult;
import shared.model.ExamScore;
import java.sql.Timestamp;
import shared.model.ExamSection;
import examiner.service.ScoreService;

// Persists theory/practical section scores and pass flags for examiner scoring flows.
public class ScoreServiceImpl implements ScoreService {

    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();

    // Creates or updates a section score row and syncs the exam result pass flag.
    @Override
    public boolean update(int candidateId, SectionType section, double score, boolean passed) {
        if (candidateId <= 0 || section == null || score < 0) {
            return false;
        }
        ExamEnrollment enrollment = enrollmentDAO.getLatestByCandidateId(candidateId);
        if (enrollment == null) {
            return false;
        }
        int examResultId = getOrCreateExamResultId(enrollment.getExamEnrollmentId(), passed);
        if (examResultId <= 0) {
            return false;
        }
        ExamSection sectionRow = sectionDAO.getBySectionType(section.getValue());
        if (sectionRow == null) {
            return false;
        }
        return upsertScore(examResultId, sectionRow.getExamSectionId(), score);
    }

    // Gets or creates an ExamResult row and updates its pass flag when it already exists.
    private int getOrCreateExamResultId(int examEnrollmentId, boolean passed) {
        int examResultId = examResultDAO.getExamResultIdByExamEnrollmentId(examEnrollmentId);
        if (examResultId > 0) {
            examResultDAO.updatePassed(examResultId, passed);
            return examResultId;
        }
        ExamResult result = new ExamResult();
        result.setExamEnrollmentId(examEnrollmentId);
        result.setPassed(passed);
        result.setResultDate(new Timestamp(System.currentTimeMillis()));
        return examResultDAO.add(result);
    }

    // Private helper: upsert score.
    private boolean upsertScore(int examResultId, int examSectionId, double score) {
        ExamScore existing = examScoreDAO.getByExamResultAndSection(examResultId, examSectionId);
        if (existing != null) {
            return examScoreDAO.updateScore(existing.getExamScoreId(), score);
        }
        ExamScore row = new ExamScore();
        row.setExamResultId(examResultId);
        row.setExamSectionId(examSectionId);
        row.setScore(score);
        return examScoreDAO.add(row) > 0;
    }
}

