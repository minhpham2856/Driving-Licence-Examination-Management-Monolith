package DAO.Impl;

import DAO.TheoryPaperDAO;
import DBConnection.DBContext;
import Models.TheoryPaperAnswer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                    OR c.CandidateNumber LIKE ?
                    OR TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = ?
                  )
            ORDER BY q.QuestionNumber
            """;

    @Override
    public List<TheoryPaperAnswer> getAnswersBySessionAndSbd(int sessionId, String sbd) {
        List<TheoryPaperAnswer> answers = new ArrayList<>();
        if (sbd == null || sbd.isBlank()) {
            return answers;
        }
        String normalized = sbd.trim();
        Integer candidateNo = parseCandidateNo(normalized);
        try (PreparedStatement ps = getConnection().prepareStatement(ANSWERS_SQL)) {
            ps.setInt(1, sessionId);
            ps.setString(2, normalized);
            ps.setString(3, "%-" + normalized.replaceAll("^.*-", "") + "%");
            if (candidateNo != null) {
                ps.setInt(4, candidateNo);
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
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

    @Override
    public int countQuestionsBySessionAndSbd(int sessionId, String sbd) {
        return getAnswersBySessionAndSbd(sessionId, sbd).size();
    }

    private TheoryPaperAnswer mapRow(ResultSet rs) throws SQLException {
        TheoryPaperAnswer row = new TheoryPaperAnswer();
        row.setQuestionNo(rs.getInt("QuestionNumber"));
        row.setImageUrl(rs.getString("ImageUrl"));
        row.setCorrectAnswer(nullToDash(rs.getString("CorrectAnswer")));
        String student = rs.getString("StudentAnswer");
        row.setStudentAnswer(student == null || student.isBlank() ? "—" : student.trim());
        String correct = rs.getString("CorrectAnswer");
        row.setCorrect(correct != null && student != null
                && correct.trim().equalsIgnoreCase(student.trim()));
        return row;
    }

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

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
