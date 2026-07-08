package dao.impl;
import java.util.*;
import dao.TheoryPaperDAO;
import dbconnection.DBContext;
import dto.score.TheoryPaperAnswerDTO;
import model.TheoryPaper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
public class TheoryPaperDAOImpl extends DBContext implements TheoryPaperDAO {
    @Override
    public TheoryPaper getByExamEnrollmentId(int examEnrollmentId) {
        String sql = "SELECT * FROM TheoryPaper WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TheoryPaper tp = new TheoryPaper();
                    tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    tp.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    tp.setExamDeviceId(rs.getInt("ExamDeviceId"));
                    tp.setStartedAt(rs.getTimestamp("StartedAt"));
                    tp.setSubmittedAt(rs.getTimestamp("SubmittedAt"));
                    return tp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<TheoryPaper> getAllByExamEnrollmentIds(List<Integer> examEnrollmentIds) {
        List<TheoryPaper> list = new ArrayList<>();
        if (examEnrollmentIds == null || examEnrollmentIds.isEmpty()) {
            return list;
        }
        StringBuilder sb = new StringBuilder("SELECT * FROM TheoryPaper WHERE ExamEnrollmentId IN (");
        for (int i = 0; i < examEnrollmentIds.size(); i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")");
        try (PreparedStatement ps = getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < examEnrollmentIds.size(); i++) {
                ps.setInt(i + 1, examEnrollmentIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TheoryPaper tp = new TheoryPaper();
                    tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    tp.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    tp.setExamDeviceId(rs.getInt("ExamDeviceId"));
                    tp.setStartedAt(rs.getTimestamp("StartedAt"));
                    tp.setSubmittedAt(rs.getTimestamp("SubmittedAt"));
                    list.add(tp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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
                    answers.add(mapAnswerRow(rs));
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

    private TheoryPaperAnswerDTO mapAnswerRow(ResultSet rs) throws SQLException {
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
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
