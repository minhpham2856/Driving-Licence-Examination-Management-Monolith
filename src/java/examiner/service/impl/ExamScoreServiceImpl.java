package examiner.service.impl;

import examiner.dao.ExamEnrollmentDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.ExamScoreDAO;
import examiner.dao.ExamSectionDAO;
import examiner.dao.impl.ExamEnrollmentDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.ExamScoreDAOImpl;
import examiner.dao.impl.ExamSectionDAOImpl;
import examiner.enums.SectionType;
import shared.model.ExamEnrollment;
import shared.model.ExamResult;
import shared.model.ExamScore;
import examiner.service.ExamScoreService;

import java.sql.Timestamp;

public class ExamScoreServiceImpl implements ExamScoreService {

    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final ExamScoreDAO examScoreDAO = new ExamScoreDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();

    @Override
    public boolean upsertTheoryCorrectCount(int candidateId, int correct, int passThreshold) {
        return upsertSectionScore(candidateId, SectionType.THEORY, correct, correct >= passThreshold);
    }

    @Override
    public boolean upsertSectionScore(int candidateId, SectionType section, double score, boolean passed) {
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
        examiner.model.ExamSection sectionRow = sectionDAO.getBySectionType(section.getValue());
        if (sectionRow == null) {
            return false;
        }
        return upsertScore(examResultId, sectionRow.getExamSectionId(), score);
    }

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

