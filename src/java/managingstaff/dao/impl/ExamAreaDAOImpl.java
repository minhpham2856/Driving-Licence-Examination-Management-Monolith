package managingstaff.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import managingstaff.dao.ExamAreaDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamArea;

public class ExamAreaDAOImpl extends DBContext implements ExamAreaDAO {
    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> rows = new ArrayList<>();
        String sql = "SELECT * FROM ExamArea WHERE (?='' OR AreaName LIKE ?)"
                + " AND (?='' OR AreaType=?) ORDER BY AreaName";
        String term = keyword == null ? "" : keyword.trim();
        String type = areaType == null ? "" : areaType.trim();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, "%" + term + "%");
            ps.setString(3, type);
            ps.setString(4, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải khu vực thi", ex);
        }
    }

    @Override
    public ExamArea findById(int examAreaId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM ExamArea WHERE ExamAreaId=?")) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải khu vực thi", ex);
        }
    }

    private ExamArea map(ResultSet rs) throws SQLException {
        return new ExamArea(rs.getInt("ExamAreaId"), rs.getString("AreaName"),
                rs.getString("AreaType"), (Integer) rs.getObject("Capacity"),
                rs.getString("Location"), rs.getInt("ExamZoneId"));
    }
}
