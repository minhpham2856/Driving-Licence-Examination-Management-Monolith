package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.TheoryPaperDAO;
import DTOs.TheoryPaperAnswerDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TheoryPaperDAOImpl implements TheoryPaperDAO {

    private final DBContext ctx;

    public TheoryPaperDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd) {
        List<TheoryPaperAnswerDTO> answers = new ArrayList<>();

        if (sbd == null || sbd.isBlank()) {
            return answers;
        }

        String normalized = sbd.trim();
        Integer candidateNo = parseCandidateNo(normalized);
        String sql = """
                select q.QuestionNumber,
                       q.ImageUrl,
                       q.CorrectAnswer,
                       ca.Answer as StudentAnswer
                from ExamEnrollment ec
                join Candidate c on c.CandidateId = ec.CandidateId
                join TheoryPaper tp on tp.ExamEnrollmentId = ec.ExamEnrollmentId
                join CandidateAnswer ca on ca.TheoryPaperId = tp.TheoryPaperId
                join Question q on q.QuestionId = ca.QuestionId
                where ec.SessionId = ?
                  and (
                        c.CandidateNumber = ?
                        or TRY_CAST(c.CandidateNumber as INT) = TRY_CAST(? as INT)
                        or c.CandidateNumber like ?
                        or TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) as INT) = ?
                      )
                order by q.QuestionNumber
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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

    @Override
    public int countQuestionsBySessionAndSbd(int sessionId, String sbd) {
        return getAnswersBySessionAndSbd(sessionId, sbd).size();
    }

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
