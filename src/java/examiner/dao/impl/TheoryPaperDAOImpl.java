package examiner.dao.impl;

import examiner.dao.TheoryPaperDAO;
import shared.dbconnection.DBContext;
import shared.enums.SectionType;
import shared.model.TheoryPaper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TheoryPaperDAOImpl extends DBContext implements TheoryPaperDAO {

    @Override
    public TheoryPaper getByExamEnrollmentId(int examEnrollmentId) {
        String sql = """
                SELECT 
                     tp.TheoryPaperId, 
                     tp.ExamEnrollmentSectionId, 
                     tp.StartedAt, 
                     tp.SubmittedAt
                FROM TheoryPaper tp
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = tp.ExamEnrollmentSectionId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, SectionType.THEORY.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private TheoryPaper mapRow(ResultSet rs) throws SQLException {
        TheoryPaper tp = new TheoryPaper();
        tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
        tp.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
        tp.setStartedAt(rs.getTimestamp("StartedAt"));
        tp.setSubmittedAt(rs.getTimestamp("SubmittedAt"));
        return tp;
    }
}
