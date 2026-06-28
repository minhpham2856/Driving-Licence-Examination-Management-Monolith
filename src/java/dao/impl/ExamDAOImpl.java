package dao.impl;

import dao.ExamDAO;
import dbconnection.DBContext;
import model.exam.Exam;

public class ExamDAOImpl extends DBContext implements ExamDAO {
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Exam";
        try (java.sql.PreparedStatement ps = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Exam findById(int examId) {
        String sql = "SELECT * FROM Exam WHERE ExamId = ?";
        try (java.sql.PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Exam e = new Exam();
                    e.setExamId(rs.getInt("ExamId"));
                    e.setExamCode(rs.getString("ExamCode"));
                    e.setExamDate(rs.getTimestamp("ExamDate"));
                    e.setCentreName(rs.getString("CentreName"));
                    e.setStatus(rs.getString("Status"));
                    e.setLicenceId(rs.getInt("LicenceId"));
                    return e;
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
