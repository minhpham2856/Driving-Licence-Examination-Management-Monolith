package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamComputerDAO;
import Models.ExamComputer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamComputerDAOImpl extends DBContext implements ExamComputerDAO {

    private static final String DEVICE_SELECT = """
            SELECT ExamDeviceId AS id, DeviceName AS computerCode, ExamAreaId AS areaId,
                   [Status] AS status, NULL AS lastUsedAt
            FROM ExamDevice
            WHERE DeviceType IN ('Computer', 'PC', N'Máy tính') OR DeviceName LIKE 'PC-%'
            """;

    @Override
    public List<ExamComputer> getAvailableComputers() {
        List<ExamComputer> list = new ArrayList<>();
        String sql = DEVICE_SELECT + " AND [Status] IN ('Available', 'Operational') ORDER BY DeviceName";
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
    public List<ExamComputer> getAvailableComputersByArea(int areaId) {
        List<ExamComputer> list = new ArrayList<>();
        String sql = DEVICE_SELECT + " AND [Status] IN ('Available', 'Operational') AND ExamAreaId = ? ORDER BY DeviceName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, areaId);
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
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE ExamDevice SET [Status] = ? WHERE ExamDeviceId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamComputer map(ResultSet rs) throws SQLException {
        ExamComputer ec = new ExamComputer();
        ec.setId(rs.getInt("id"));
        ec.setComputerCode(rs.getString("computerCode"));
        ec.setAreaId(rs.getInt("areaId"));
        ec.setStatus(rs.getString("status"));
        ec.setLastUsedAt(rs.getTimestamp("lastUsedAt"));
        return ec;
    }
}
