package dao.impl;

import java.sql.*;

import dao.ExamDAO;
import dbconnection.DBContext;
import model.Exam;

public class ExamDAOImpl extends DBContext implements ExamDAO {
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Exam";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Exam getById(int examId) {
        String sql = "SELECT * FROM Exam WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
