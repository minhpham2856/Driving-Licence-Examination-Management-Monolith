package candidate.dao.impl;

import candidate.dao.CandidateExamAccessDAO;
import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import shared.dbconnection.DBContext;
import shared.enums.CandidateStatus;
import shared.enums.ExamStatus;
import shared.enums.SectionType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import shared.model.Question;

public class CandidateExamAccessDAOImpl extends DBContext implements CandidateExamAccessDAO {

    @Override
    public CandidateExamContextDTO getEligibleTheoryContext(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return null;
        }
        String sql = """
                SELECT TOP 1 c.CandidateId, c.CandidateNumber, c.FullName,
                       ee.ExamId, ee.ExamEnrollmentId,
                       ees.ExamEnrollmentSectionId, ees.ExamSectionId, ees.ExamAreaId,
                       es.LicenceId, es.DurationMinutes
                FROM Candidate c
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Exam e ON e.ExamId = ee.ExamId
                INNER JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE c.CandidateNumber = ?
                  AND c.IsAbsent = 0 AND c.IsSuspended = 0
                  AND e.Status = ?
                  AND es.SectionType = ?
                  AND ees.ExamAreaId IS NOT NULL
                  AND ees.Status = ?
                ORDER BY e.ExamDate DESC, ees.ExamEnrollmentSectionId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, candidateNumber.trim());
            ps.setString(2, ExamStatus.IN_PROGRESS.getValue());
            ps.setString(3, SectionType.THEORY.getValue());
            ps.setString(4, CandidateStatus.IN_PROGRESS.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private CandidateExamContextDTO map(ResultSet rs) throws SQLException {
        CandidateExamContextDTO context = new CandidateExamContextDTO();
        context.setCandidateId(rs.getInt("CandidateId"));
        context.setCandidateNumber(rs.getString("CandidateNumber"));
        context.setFullName(rs.getString("FullName"));
        context.setExamId(rs.getInt("ExamId"));
        context.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        context.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
        context.setExamSectionId(rs.getInt("ExamSectionId"));
        context.setExamAreaId(rs.getInt("ExamAreaId"));
        context.setLicenceId(rs.getInt("LicenceId"));
        int duration = rs.getInt("DurationMinutes");
        context.setDurationMinutes(rs.wasNull() || duration <= 0 ? 20 : duration);
        return context;
    }

    @Override
    public int startTheoryPaper(int examEnrollmentSectionId) {
        String find = "SELECT TOP 1 TheoryPaperId FROM TheoryPaper "
                + "WHERE ExamEnrollmentSectionId = ? AND SubmittedAt IS NULL ORDER BY TheoryPaperId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(find)) {
            ps.setInt(1, examEnrollmentSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examEnrollmentSectionId);
            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Question> getRandomQuestions(int licenceId, int limit) {
        List<Question> pool = new ArrayList<>();
        String sql = "SELECT q.QuestionId, q.QuestionNumber, q.ImageUrl, q.CorrectAnswer, "
                + "q.IsCritical, q.QuestionCategoryId FROM Question q "
                + "INNER JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId WHERE lq.LicenceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("QuestionId"));
                    q.setQuestionNumber(rs.getInt("QuestionNumber"));
                    q.setImageUrl(rs.getString("ImageUrl"));
                    q.setCorrectAnswer(rs.getString("CorrectAnswer"));
                    q.setCritical(rs.getBoolean("IsCritical"));
                    q.setQuestionCategoryId(rs.getInt("QuestionCategoryId"));
                    pool.add(q);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Collections.shuffle(pool);
        List<Question> selected = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (Question question : pool) {
            if (question.isCritical()) {
                selected.add(question);
                ids.add(question.getQuestionId());
                break;
            }
        }
        for (Question question : pool) {
            if (selected.size() >= limit) break;
            if (ids.add(question.getQuestionId())) selected.add(question);
        }
        return selected;
    }

    @Override
    public CandidateExamResultDTO submit(int theoryPaperId, CandidateExamContextDTO context,
            Map<Integer, String> answers) {
        CandidateExamResultDTO result = grade(context.getQuestions(), answers);
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            replaceAnswers(connection, theoryPaperId, context.getQuestions(), answers);
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE TheoryPaper SET SubmittedAt = GETDATE() WHERE TheoryPaperId = ?")) {
                ps.setInt(1, theoryPaperId);
                ps.executeUpdate();
            }
            int examResultId = upsertResult(connection, context.getExamEnrollmentId(), result.isPassed());
            upsertScore(connection, examResultId, context.getExamSectionId(),
                    context.getQuestions().isEmpty() ? 0
                            : result.getCorrect() * 100.0 / context.getQuestions().size());
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ExamEnrollmentSection SET Status = ? WHERE ExamEnrollmentSectionId = ?")) {
                ps.setString(1, CandidateStatus.AWAITING_SIGNATURE.getValue());
                ps.setInt(2, context.getExamEnrollmentSectionId());
                ps.executeUpdate();
            }
            connection.commit();
            return result;
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            ex.printStackTrace();
            return null;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private CandidateExamResultDTO grade(List<Question> questions, Map<Integer, String> answers) {
        CandidateExamResultDTO result = new CandidateExamResultDTO();
        for (Question question : questions) {
            String answer = answers.get(question.getQuestionId());
            if (answer == null || answer.isBlank()) {
                result.setUnanswered(result.getUnanswered() + 1);
            } else if (answer.trim().equalsIgnoreCase(question.getCorrectAnswer())) {
                result.setCorrect(result.getCorrect() + 1);
            } else {
                result.setWrong(result.getWrong() + 1);
            }
            if (question.isCritical()
                    && (answer == null || !answer.trim().equalsIgnoreCase(question.getCorrectAnswer()))) {
                result.setCriticalFailed(true);
            }
        }
        int required = (int) Math.ceil(questions.size() * 0.8);
        result.setPassed(result.getCorrect() >= required && !result.isCriticalFailed());
        return result;
    }

    private void replaceAnswers(Connection connection, int paperId, List<Question> questions,
            Map<Integer, String> answers) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM CandidateAnswer WHERE TheoryPaperId = ?")) {
            delete.setInt(1, paperId);
            delete.executeUpdate();
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer) VALUES (?, ?, ?)")) {
            for (Question question : questions) {
                insert.setInt(1, paperId);
                insert.setInt(2, question.getQuestionId());
                String answer = answers.get(question.getQuestionId());
                if (answer == null || answer.isBlank()) insert.setNull(3, Types.NVARCHAR);
                else insert.setString(3, answer.trim());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private int upsertResult(Connection connection, int enrollmentId, boolean passed) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement(
                "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?")) {
            find.setInt(1, enrollmentId);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE ExamResult SET IsPassed = ?, ResultDate = GETDATE() WHERE ExamResultId = ?")) {
                        update.setBoolean(1, passed);
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                    return id;
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setInt(1, enrollmentId);
            insert.setBoolean(2, passed);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private void upsertScore(Connection connection, int resultId, int sectionId, double score)
            throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE ExamScore SET Score = ? WHERE ExamResultId = ? AND ExamSectionId = ?")) {
            update.setDouble(1, score);
            update.setInt(2, resultId);
            update.setInt(3, sectionId);
            if (update.executeUpdate() > 0) return;
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)")) {
            insert.setInt(1, resultId);
            insert.setInt(2, sectionId);
            insert.setDouble(3, score);
            insert.executeUpdate();
        }
    }
}
