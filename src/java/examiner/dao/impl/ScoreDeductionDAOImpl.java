package examiner.dao.impl;

import examiner.dao.ScoreDeductionDAO;
import shared.dbconnection.DBContext;
import shared.model.ScoreDeduction;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// JDBC implementation for ScoreDeduction; examiner module DAO layer only.
public class ScoreDeductionDAOImpl extends DBContext implements ScoreDeductionDAO {

    private static final String BASE_SELECT =
            "SELECT ScoreDeductionId, LicenceId, Reason, Points, IsCritical, ExamSectionId FROM ScoreDeduction";

    // Loads one score deduction rule row by primary key.
    @Override
    public ScoreDeduction get(int scoreDeductionId) {
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

    // Private helper: map.
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

