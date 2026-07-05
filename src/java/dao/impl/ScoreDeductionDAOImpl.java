package dao.impl;

import dao.ScoreDeductionDAO;
import dbconnection.DBContext;
import model.ScoreDeduction;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScoreDeductionDAOImpl extends DBContext implements ScoreDeductionDAO {

    private static final String BASE_SELECT =
            "SELECT ScoreDeductionId, LicenceId, Reason, Points, IsCritical, ExamSectionId FROM ScoreDeduction";

    @Override
    public ScoreDeduction getById(int scoreDeductionId) {
        String sql = BASE_SELECT + " WHERE ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, scoreDeductionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ScoreDeduction map(ResultSet rs) throws SQLException {
        ScoreDeduction deduction = new ScoreDeduction();
        deduction.setScoreDeductionId(rs.getInt("ScoreDeductionId"));
        deduction.setLicenceId(rs.getInt("LicenceId"));
        deduction.setReason(rs.getString("Reason"));
        deduction.setPoints(rs.getDouble("Points"));
        deduction.setCritical(rs.getBoolean("IsCritical"));
        deduction.setExamSectionId(rs.getInt("ExamSectionId"));
        return deduction;
    }
}
