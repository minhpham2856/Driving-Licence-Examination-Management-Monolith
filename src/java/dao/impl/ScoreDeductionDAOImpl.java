package dao.impl;

import dao.ScoreDeductionDAO;
import dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.exam.ScoreDeduction;

public class ScoreDeductionDAOImpl extends DBContext implements ScoreDeductionDAO {

    private ScoreDeduction map(ResultSet rs) throws SQLException {
        ScoreDeduction sd = new ScoreDeduction();
        sd.setScoreDeductionId(rs.getInt("ScoreDeductionId"));
        sd.setReason(rs.getString("Reason"));
        sd.setPoints(rs.getDouble("Points"));
        sd.setCritical(rs.getBoolean("IsCritical"));
        
        int sectionId = rs.getInt("ExamSectionId");
        if (!rs.wasNull()) {
            sd.setExamSectionId(sectionId);
        }
        sd.setSortOrder(rs.getInt("SortOrder"));
        return sd;
    }

    @Override
    public List<ScoreDeduction> findAll() {
        List<ScoreDeduction> list = new ArrayList<>();
        String sql = "SELECT * FROM ScoreDeduction ORDER BY ScoreDeductionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ScoreDeduction> findBySectionId(int examSectionId) {
        List<ScoreDeduction> list = new ArrayList<>();
        if (examSectionId <= 0) return list;
        String sql = "SELECT * FROM ScoreDeduction WHERE ExamSectionId = ? ORDER BY SortOrder, ScoreDeductionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

