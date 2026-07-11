package examiner.dao.impl;

import examiner.dao.ExamSectionDAO;
import dbconnection.DBContext;
import examiner.model.ExamSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExamSectionDAOImpl extends DBContext implements ExamSectionDAO {

    private static final String BASE_SELECT = "SELECT ExamSectionId, SectionType, LicenceId, DurationMinutes, ExamId FROM ExamSection";

    @Override
    public ExamSection getById(int examSectionId) {
        String sql = BASE_SELECT + " WHERE ExamSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examSectionId);
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

    @Override
    public ExamSection getBySectionType(String sectionType) {
        String sql = BASE_SELECT + " WHERE SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, sectionType);
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

    private static ExamSection map(ResultSet rs) throws SQLException {
        ExamSection section = new ExamSection();
        section.setExamSectionId(rs.getInt("ExamSectionId"));
        section.setSectionType(rs.getString("SectionType"));
        section.setLicenceId(rs.getInt("LicenceId"));
        int duration = rs.getInt("DurationMinutes");
        if (!rs.wasNull()) {
            section.setDurationMinutes(duration);
        }
        section.setExamId(rs.getInt("ExamId"));
        return section;
    }
}
