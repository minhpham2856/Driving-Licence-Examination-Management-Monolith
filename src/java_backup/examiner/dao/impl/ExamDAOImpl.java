package examiner.dao.impl;

import examiner.enums.ExamStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import examiner.dao.ExamDAO;
import shared.dbconnection.DBContext;
import shared.model.Exam;

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
                    return mapExam(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Exam> getByStatus(ExamStatus status) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT * FROM [Exam] WHERE [Status] = ? ORDER BY StartTime DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Exam> getExamsByExaminerId(int examinerId) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT e.* FROM [Exam] e JOIN ExaminerSchedule s ON s.ExamId = e.ExamId "
                + "WHERE s.ExaminerId = ? ORDER BY e.StartTime DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Exam mapExam(ResultSet rs) throws SQLException {
        Exam e = new Exam();
        e.setExamId(rs.getInt("ExamId"));
        e.setExamCode(rs.getString("ExamCode"));
        e.setExamDate(rs.getTimestamp("ExamDate"));
        e.setStartTime(rs.getTimestamp("StartTime"));
        e.setEndTime(rs.getTimestamp("EndTime"));
        e.setCentreName(rs.getString("CentreName"));
        e.setStatus(rs.getString("Status"));
        e.setLicenceId(rs.getInt("LicenceId"));
        return e;
    }
}

