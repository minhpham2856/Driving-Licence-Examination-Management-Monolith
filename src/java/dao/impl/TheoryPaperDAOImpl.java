package dao.impl;


import dao.TheoryPaperDAO;

import dbconnection.DBContext;

import dto.score.TheoryPaperAnswerDTO;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of TheoryPaperDAO for retrieving candidate theory paper
 * answers and matching them against correct answers for scoring.
 */
public class TheoryPaperDAOImpl extends DBContext implements TheoryPaperDAO {

    private static final String ANSWERS_SQL = """
            SELECT q.QuestionNumber,
                   q.ImageUrl,
                   q.CorrectAnswer,
                   ca.Answer AS StudentAnswer
            FROM Exam_Candidate ec
            INNER JOIN Candidate c ON c.CandidateId = ec.CandidateId
            INNER JOIN TheoryPaper tp ON tp.ExamCandidateId = ec.ExamCandidateId
            INNER JOIN CandidateAnswer ca ON ca.TheoryPaperId = tp.TheoryPaperId
            INNER JOIN Question q ON q.QuestionId = ca.QuestionId
            WHERE ec.SessionId = ?
              AND (
                    c.CandidateNumber = ?
                    OR TRY_CAST(c.CandidateNumber AS INT) = TRY_CAST(? AS INT)
                    OR c.CandidateNumber LIKE ?
                    OR TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = ?
                  )
            ORDER BY q.QuestionNumber
            """;

    /**
     * Retrieves all answers (question number, student answer, correct answer)
     * for a candidate identified by SBD (số báo danh) within a given session.
     * Supports multiple SBD formats (raw number, hyphenated code).
     *
     * @param sessionId the SessionId
     * @param sbd       the candidate number (e.g. "B-001" or "1")
     * @return list of TheoryPaperAnswerDTO with correctness evaluation
     */
    @Override
    public List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd) {
        List<TheoryPaperAnswerDTO> answers = new ArrayList<>();
        if (sbd == null || sbd.isBlank()) {
            return answers;
        }
        String normalized = sbd.trim();
        Integer candidateNo = parseCandidateNo(normalized);
        try (PreparedStatement ps = getConnection().prepareStatement(ANSWERS_SQL)) {
            ps.setInt(1, sessionId);
            ps.setString(2, normalized);
            ps.setString(3, normalized);
            ps.setString(4, "%-" + normalized.replaceAll("^.*-", "") + "%");
            if (candidateNo != null) {
                ps.setInt(5, candidateNo);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /**
     * Counts the number of answered questions for a candidate in a session.
     *
     * @param sessionId the SessionId
     * @param sbd       the candidate number
     * @return the total question count
     */
    @Override
    public int countQuestionsBySessionAndSbd(int sessionId, String sbd) {
        return getAnswersBySessionAndSbd(sessionId, sbd).size();
    }

    /** Maps a ResultSet row to a TheoryPaperAnswerDTO, normalising blank answers to "-". */
    private TheoryPaperAnswerDTO mapRow(ResultSet rs) throws SQLException {
        TheoryPaperAnswerDTO row = new TheoryPaperAnswerDTO();
        row.setQuestionNo(rs.getInt("QuestionNumber"));
        row.setImageUrl(rs.getString("ImageUrl"));
        row.setCorrectAnswer(nullToDash(rs.getString("CorrectAnswer")));
        String student = rs.getString("StudentAnswer");
        row.setStudentAnswer(student == null || student.isBlank() ? "-" : student.trim());
        String correct = rs.getString("CorrectAnswer");
        row.setCorrect(correct != null && student != null
                && correct.trim().equalsIgnoreCase(student.trim()));
        return row;
    }

    /** Extracts the numeric portion from an SBD string (e.g. "B-001" -> 1). */
    private static Integer parseCandidateNo(String sbd) {
        try {
            if (sbd.contains("-")) {
                return Integer.parseInt(sbd.split("-")[1]);
            }
            return Integer.parseInt(sbd);
        } catch (Exception e) {
            return null;
        }
    }

    /** Converts null or blank strings to "-" for display safety. */
    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
