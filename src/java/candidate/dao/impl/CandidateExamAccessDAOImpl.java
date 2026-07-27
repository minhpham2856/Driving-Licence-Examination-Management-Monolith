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
import java.util.List;
import java.util.Map;
import shared.model.Question;
import shared.util.PasswordUtil;

public class CandidateExamAccessDAOImpl extends DBContext implements CandidateExamAccessDAO {

    @Override
    public int findActiveExamIdForLogin(String examCodeOrId, String examPassword) {
        if (examCodeOrId == null || examCodeOrId.isBlank()
                || examPassword == null || examPassword.isBlank()) {
            return 0;
        }
        String loginCode = examCodeOrId.trim();
        Integer numericExamId = parseNumericSbd(loginCode);
        String sql = """
                SELECT TOP 1 ExamId, ExamPassword
                FROM Exam
                WHERE [Status] = ?
                  AND (ExamCode = ? OR (? IS NOT NULL AND ExamId = ?))
                ORDER BY ExamDate DESC, ExamId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, ExamStatus.IN_PROGRESS.getValue());
            ps.setString(2, loginCode);
            if (numericExamId == null) {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(3, numericExamId);
                ps.setInt(4, numericExamId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }
                return passwordMatches(examPassword.trim(), rs.getString("ExamPassword"))
                        ? rs.getInt("ExamId")
                        : 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public CandidateExamContextDTO getEligibleTheoryContext(int examId, String candidateNumber) {
        if (examId <= 0) {
            return null;
        }
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return null;
        }
        String normalizedSbd = candidateNumber.trim();
        Integer numericSbd = parseNumericSbd(normalizedSbd);
        String sql = """
                SELECT TOP 1 c.CandidateId, c.CandidateNumber, c.FullName, c.GovernmentIdNumber,
                       ee.ExamId, ee.ExamEnrollmentId,
                       ees.ExamEnrollmentSectionId, ees.ExamSectionId,
                       COALESCE(ees.ExamAreaId, scheduleArea.ExamAreaId) AS ExamAreaId,
                       es.LicenceId, es.DurationMinutes,
                       e.ExamCode,
                       CONVERT(VARCHAR(10), e.ExamDate, 103) AS ExamDateDisplay,
                       l.LicenceClass,
                       es.SectionType
                FROM Candidate c
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Exam e ON e.ExamId = ee.ExamId
                INNER JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                LEFT JOIN Licence l ON l.LicenceId = es.LicenceId
                OUTER APPLY (
                    SELECT TOP 1 x.ExamAreaId
                    FROM ExaminerSchedule x
                    WHERE x.ExamId = ee.ExamId
                      AND x.ExamSectionId = ees.ExamSectionId
                      AND x.ExamAreaId IS NOT NULL
                    ORDER BY x.ExaminerScheduleId
                ) scheduleArea
                WHERE ee.ExamId = ?
                  AND (c.CandidateNumber = ? OR (? IS NOT NULL AND TRY_CAST(c.CandidateNumber AS INT) = ?))
                  AND c.IsAbsent = 0 AND c.IsSuspended = 0
                  AND e.Status = ?
                  AND es.SectionType = ?
                  AND COALESCE(ees.ExamAreaId, scheduleArea.ExamAreaId) IS NOT NULL
                  AND ees.CheckedInAt IS NOT NULL
                  AND ees.Status IN (?, ?)
                ORDER BY e.ExamDate DESC, ees.ExamEnrollmentSectionId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, normalizedSbd);
            if (numericSbd == null) {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(3, numericSbd);
                ps.setInt(4, numericSbd);
            }
            ps.setString(5, ExamStatus.IN_PROGRESS.getValue());
            ps.setString(6, SectionType.THEORY.getValue());
            ps.setString(7, CandidateStatus.NOT_STARTED.getValue());
            ps.setString(8, CandidateStatus.IN_PROGRESS.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        String stored = storedPassword.trim();
        try {
            if (PasswordUtil.matches(rawPassword, stored)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
            // Plain text seed values are allowed for the exam kiosk password.
        }
        return rawPassword.equals(stored);
    }

    private Integer parseNumericSbd(String value) {
        if (value == null || !value.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private CandidateExamContextDTO map(ResultSet rs) throws SQLException {
        CandidateExamContextDTO context = new CandidateExamContextDTO();
        context.setCandidateId(rs.getInt("CandidateId"));
        context.setCandidateNumber(rs.getString("CandidateNumber"));
        context.setFullName(rs.getString("FullName"));
        context.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        context.setExamId(rs.getInt("ExamId"));
        context.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        context.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
        context.setExamSectionId(rs.getInt("ExamSectionId"));
        context.setExamAreaId(rs.getInt("ExamAreaId"));
        context.setLicenceId(rs.getInt("LicenceId"));
        context.setExamCode(rs.getString("ExamCode"));
        context.setLicenceClass(rs.getString("LicenceClass"));
        context.setExamDateDisplay(rs.getString("ExamDateDisplay"));
        context.setSectionName(rs.getString("SectionType"));
        int duration = rs.getInt("DurationMinutes");
        context.setDurationMinutes(rs.wasNull() || duration <= 0 ? 20 : duration);
        return context;
    }

    @Override
    public boolean startTheoryAttempt(CandidateExamContextDTO context, int questionLimit) {
        if (context == null || context.getExamEnrollmentSectionId() <= 0 || questionLimit <= 0) {
            return false;
        }
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            int paperId = findOpenTheoryPaper(connection, context.getExamEnrollmentSectionId());
            if (paperId <= 0) {
                paperId = insertTheoryPaper(connection, context.getExamEnrollmentSectionId());
            }
            if (paperId <= 0) {
                connection.rollback();
                return false;
            }
            markSectionStarted(connection, context.getExamEnrollmentSectionId());
            List<Question> questions = getQuestionsByPaper(connection, paperId);
            if (questions.isEmpty()) {
                questions = chooseRandomQuestions(connection, context.getLicenceId(), questionLimit);
                if (questions.isEmpty() || !insertQuestionPlaceholders(connection, paperId, questions)) {
                    connection.rollback();
                    return false;
                }
            }
            connection.commit();
            context.setTheoryPaperId(paperId);
            context.setQuestions(questions);
            context.setStartedAtMillis(System.currentTimeMillis());
            return true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            ex.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private int findOpenTheoryPaper(Connection connection, int examEnrollmentSectionId) throws SQLException {
        String sql = "SELECT TOP 1 TheoryPaperId FROM TheoryPaper "
                + "WHERE ExamEnrollmentSectionId = ? AND SubmittedAt IS NULL ORDER BY TheoryPaperId DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insertTheoryPaper(Connection connection, int examEnrollmentSectionId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examEnrollmentSectionId);
            if (ps.executeUpdate() != 1) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private void markSectionStarted(Connection connection, int examEnrollmentSectionId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ExamEnrollmentSection SET Status = ?, StartedAt = COALESCE(StartedAt, GETDATE()) "
                + "WHERE ExamEnrollmentSectionId = ? AND Status IN (?, ?)")) {
            ps.setString(1, CandidateStatus.IN_PROGRESS.getValue());
            ps.setInt(2, examEnrollmentSectionId);
            ps.setString(3, CandidateStatus.NOT_STARTED.getValue());
            ps.setString(4, CandidateStatus.IN_PROGRESS.getValue());
            ps.executeUpdate();
        }
    }

    private List<Question> getQuestionsByPaper(Connection connection, int paperId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.QuestionId, q.QuestionNumber, q.ImageUrl, q.CorrectAnswer, "
                + "q.IsCritical, q.QuestionCategoryId "
                + "FROM CandidateAnswer ca "
                + "JOIN Question q ON q.QuestionId = ca.QuestionId "
                + "WHERE ca.TheoryPaperId = ? ORDER BY ca.CandidateAnswerId";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, paperId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapQuestion(rs));
                }
            }
        }
        return questions;
    }

    private List<Question> chooseRandomQuestions(Connection connection, int licenceId, int limit)
            throws SQLException {
        List<Question> pool = new ArrayList<>();
        String sql = """
                    SELECT 
                        q.QuestionId, 
                        q.QuestionNumber, 
                        q.ImageUrl, 
                        q.CorrectAnswer, 
                        q.IsCritical, 
                        q.QuestionCategoryId 
                    FROM Question q 
                    JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId 
                    WHERE lq.LicenceId = ?
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pool.add(mapQuestion(rs));
                }
            }
        }

        List<Question> criticalPool = new ArrayList<>();
        List<Question> cat1Pool = new ArrayList<>();
        List<Question> cat2Pool = new ArrayList<>();
        List<Question> cat34Pool = new ArrayList<>();
        List<Question> cat5Pool = new ArrayList<>();
        List<Question> cat6Pool = new ArrayList<>();

        for (Question q : pool) {
            if (q.isCritical()) {
                criticalPool.add(q);
            } else {
                int catId = q.getQuestionCategoryId();
                switch (catId) {
                    case 1 ->
                        cat1Pool.add(q);
                    case 2 ->
                        cat2Pool.add(q);
                    case 3, 4 ->
                        cat34Pool.add(q);
                    case 5 ->
                        cat5Pool.add(q);
                    case 6 ->
                        cat6Pool.add(q);
                    default -> {
                    }
                }
            }
        }

        Collections.shuffle(criticalPool);
        Collections.shuffle(cat1Pool);
        Collections.shuffle(cat2Pool);
        Collections.shuffle(cat34Pool);
        Collections.shuffle(cat5Pool);
        Collections.shuffle(cat6Pool);

        List<Question> selected = new ArrayList<>();

        // 1 critical question
        addUpTo(selected, criticalPool, 1);
        // 8 from chapter I
        addUpTo(selected, cat1Pool, 8);
        // 1 from chapter II
        addUpTo(selected, cat2Pool, 1);
        // 1 from chapter III/IV
        addUpTo(selected, cat34Pool, 1);
        // 8 from chapter V
        addUpTo(selected, cat5Pool, 8);
        // 6 from chapter VI
        addUpTo(selected, cat6Pool, 6);

        // Fallback in case there are not enough questions in specific categories
        if (selected.size() < limit) {
            List<Question> remaining = new ArrayList<>(pool);
            remaining.removeAll(selected);
            Collections.shuffle(remaining);
            int needed = limit - selected.size();
            addUpTo(selected, remaining, needed);
        }

        Collections.shuffle(selected);
        return selected;
    }

    private void addUpTo(List<Question> selected, List<Question> source, int count) {
        int added = 0;
        for (Question q : source) {
            if (added >= count) {
                break;
            }
            selected.add(q);
            added++;
        }
    }

    private boolean insertQuestionPlaceholders(Connection connection, int paperId, List<Question> questions)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer) VALUES (?, ?, NULL)")) {
            for (Question question : questions) {
                ps.setInt(1, paperId);
                ps.setInt(2, question.getQuestionId());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        }
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("QuestionId"));
        q.setQuestionNumber(rs.getInt("QuestionNumber"));
        q.setImageUrl(rs.getString("ImageUrl"));
        q.setCorrectAnswer(rs.getString("CorrectAnswer"));
        q.setCritical(rs.getBoolean("IsCritical"));
        q.setQuestionCategoryId(rs.getInt("QuestionCategoryId"));
        return q;
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
            double finalScore = result.isCriticalFailed() ? 0 : result.getCorrect();
            upsertScore(connection, examResultId, context.getExamSectionId(),
                    finalScore);
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ExamEnrollmentSection SET Status = ? WHERE ExamEnrollmentSectionId = ?")) {
                ps.setString(1, CandidateStatus.AWAITING_SIGNATURE.getValue());
                ps.setInt(2, context.getExamEnrollmentSectionId());
                ps.executeUpdate();
            }
            connection.commit();
            return result;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            ex.printStackTrace();
            return null;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
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
        int required = 21;
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
                if (answer == null || answer.isBlank()) {
                    insert.setNull(3, Types.NVARCHAR);
                } else {
                    insert.setString(3, answer.trim());
                }
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
            if (update.executeUpdate() > 0) {
                return;
            }
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
