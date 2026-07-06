package dao.impl;

import dao.ExamSectionDAO;
import dbconnection.DBContext;
import model.ExamSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExamSectionDAOImpl extends DBContext implements ExamSectionDAO {

    private static final String BASE_SELECT = "SELECT ExamSectionId, SectionName FROM ExamSection";

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
    public ExamSection getBySectionName(String sectionName) {
        String sql = BASE_SELECT + " WHERE SectionName = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, sectionName);
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
        section.setSectionName(rs.getString("SectionName"));
        return section;
    }
}
