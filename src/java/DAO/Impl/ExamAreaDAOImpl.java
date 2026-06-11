package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamAreaDAO;
import Models.ExamArea;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamAreaDAOImpl extends DBContext implements ExamAreaDAO {

    private static final String AREA_SELECT = """
            SELECT ExamAreaId AS id, AreaName AS areaName, AreaType AS areaType,
                   Capacity AS capacity, [Location] AS location, CAST(1 AS BIT) AS isActive
            FROM ExamArea
            """;

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        List<ExamArea> list = new ArrayList<>();
        String sql = AREA_SELECT + " WHERE AreaType = 'Room' ORDER BY ExamAreaId";
        try (PreparedStatement ps = connection.prepareStatement(sql);
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
    public List<ExamArea> getAllActiveAreas() {
        List<ExamArea> list = new ArrayList<>();
        String sql = AREA_SELECT + " ORDER BY AreaType, AreaName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
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
    public List<ExamArea> getAreasBySessionId(int sessionId) {
        List<ExamArea> list = new ArrayList<>();
        String sql = """
                SELECT ea.ExamAreaId AS id, ea.AreaName AS areaName, ea.AreaType AS areaType,
                       ea.Capacity AS capacity, ea.[Location] AS location, CAST(1 AS BIT) AS isActive
                FROM Session_ExamArea sea
                JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId
                WHERE sea.SessionId = ?
                ORDER BY ea.AreaType, ea.AreaName
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    @Override
    public boolean isAreaInSession(int sessionId, int areaId) {
        String sql = "SELECT 1 FROM Session_ExamArea WHERE SessionId = ? AND ExamAreaId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ExamArea getById(int id) {
        String sql = AREA_SELECT + " WHERE ExamAreaId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
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
    public ExamArea getAreaByComputerCode(String computerCode) {
        String sql = """
                SELECT ea.ExamAreaId AS id, ea.AreaName AS areaName, ea.AreaType AS areaType,
                       ea.Capacity AS capacity, ea.[Location] AS location, CAST(1 AS BIT) AS isActive
                FROM ExamArea ea
                JOIN ExamDevice ed ON ed.ExamAreaId = ea.ExamAreaId
                WHERE ed.DeviceName = ? OR ed.DeviceName LIKE ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, computerCode);
            ps.setString(2, "%" + computerCode + "%");
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

    private ExamArea map(ResultSet rs) throws SQLException {
        ExamArea ea = new ExamArea();
        ea.setId(rs.getInt("id"));
        ea.setAreaName(rs.getString("areaName"));
        ea.setAreaType(rs.getString("areaType"));
        ea.setCapacity(rs.getInt("capacity"));
        ea.setLocation(rs.getString("location"));
        ea.setActive(rs.getBoolean("isActive"));
        return ea;
    }
}
