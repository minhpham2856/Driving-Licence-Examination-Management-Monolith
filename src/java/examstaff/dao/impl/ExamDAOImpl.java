package examstaff.dao.impl;
import examstaff.enums.ExamStatus;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import examstaff.dao.ExamDAO;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Exam> getExamsByExaminerId(int examinerId) {
        return new ArrayList<>();
    }

    @Override
    public List<Exam> getByStatus(ExamStatus status) {
        return new ArrayList<>();
    }
}
