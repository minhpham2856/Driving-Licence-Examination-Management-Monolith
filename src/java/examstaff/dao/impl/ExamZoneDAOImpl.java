package examstaff.dao.impl;

import examstaff.dao.ExamZoneDAO;
import dbconnection.DBContext;
import examstaff.model.ExamZone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamZoneDAOImpl implements ExamZoneDAO {

    private ExamZone map(ResultSet rs) throws SQLException {
        ExamZone zone = new ExamZone();
        zone.setExamZoneId(rs.getInt("ExamZoneId"));
        zone.setZoneName(rs.getString("ZoneName"));
        zone.setLocation(rs.getString("Location"));
        zone.setActive(rs.getBoolean("IsActive"));
        return zone;
    }

    @Override
    public List<ExamZone> findAllActive() {
        List<ExamZone> list = new ArrayList<>();
        String sql = "SELECT ExamZoneId, ZoneName, [Location], IsActive FROM ExamZone "
                + "WHERE IsActive = 1 ORDER BY ZoneName";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ExamZone getById(int examZoneId) {
        String sql = "SELECT ExamZoneId, ZoneName, [Location], IsActive FROM ExamZone WHERE ExamZoneId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examZoneId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
