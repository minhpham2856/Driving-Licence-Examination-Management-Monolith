package examiner.dao.impl;

import examiner.dao.ExamSectionDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation for ExamSection; examiner module DAO layer only.
public class ExamSectionDAOImpl extends DBContext implements ExamSectionDAO {

    private static final String BASE_SELECT =
            "SELECT ExamSectionId, SectionType, LicenceId, DurationMinutes, ExamId FROM ExamSection";

    // Loads one exam section row by primary key.
    @Override
    public ExamSection get(int examSectionId) {
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

    // Loads the first exam section row matching a section type string.
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

    // Lists all exam section rows defined for one exam day.
    @Override
    public List<ExamSection> getAllByExamId(int examId) {
        List<ExamSection> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE ExamId = ? ORDER BY ExamSectionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Private helper: map.
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
