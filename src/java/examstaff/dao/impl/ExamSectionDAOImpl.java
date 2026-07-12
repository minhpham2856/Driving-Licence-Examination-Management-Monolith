package examstaff.dao.impl;
import examstaff.dao.ExamSectionDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class ExamSectionDAOImpl extends DBContext implements ExamSectionDAO {
    private static final String BASE_SELECT = "SELECT ExamSectionId, SectionName FROM ExamSection";
    @Override
    public ExamSection findById(int examSectionId) {
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
    public List<ExamSection> findAll() {
        List<ExamSection> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY ExamSectionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public List<ExamSection> findByExamId(int sessionId) {
        List<ExamSection> list = new ArrayList<>();
        String sql = BASE_SELECT
                + " INNER JOIN Session_ExamSection ses ON ses.ExamSectionId = ExamSection.ExamSectionId"
                + " WHERE ses.SessionId = ?"
                + " ORDER BY ExamSection.ExamSectionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
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
    private static ExamSection map(ResultSet rs) throws SQLException {
        ExamSection section = new ExamSection();
        section.setExamSectionId(rs.getInt("ExamSectionId"));
        section.setSectionType(rs.getString("SectionName"));
        return section;
    }

    // --- mainTest-only methods ---

    @Override
    public ExamSection getById(int examSectionId) {
        return findById(examSectionId);
    }

    @Override
    public ExamSection getBySectionType(String sectionType) {
        String sql = BASE_SELECT + " WHERE SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, sectionType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            // Fallback: tÃ¬m theo SectionName náº¿u schema khÃ´ng cÃ³ SectionType
            String sql2 = BASE_SELECT + " WHERE SectionName LIKE ?";
            try (PreparedStatement ps2 = getConnection().prepareStatement(sql2)) {
                ps2.setString(1, "%" + sectionType + "%");
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) return map(rs2);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}


