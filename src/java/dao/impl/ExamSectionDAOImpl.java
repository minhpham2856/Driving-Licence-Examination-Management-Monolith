package dao.impl;
import dao.ExamSectionDAO;
import dbconnection.DBContext;
import model.ExamSection;
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
        section.setSectionName(rs.getString("SectionName"));
        return section;
    }
}
