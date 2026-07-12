package examiner.dao.impl;
import java.util.*;
import examiner.dao.TheoryPaperDAO;
import shared.dbconnection.DBContext;
import shared.model.TheoryPaper;
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
}

