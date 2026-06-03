package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamComputerDAO;
import Models.ExamComputer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamComputerDAOImpl extends DBContext implements ExamComputerDAO {

    @Override
    public List<ExamComputer> getAvailableComputers() {
        List<ExamComputer> list = new ArrayList<>();
        String sql = "select * from ExamComputer where status = 'Available' order by computerCode";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ExamComputer ec = new ExamComputer();
                ec.setId(rs.getInt("id"));
                ec.setComputerCode(rs.getString("computerCode"));
                ec.setAreaId(rs.getInt("areaId"));
                ec.setStatus(rs.getString("status"));
                ec.setLastUsedAt(rs.getTimestamp("lastUsedAt"));
                list.add(ec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamComputer> getAvailableComputersByArea(int areaId) {
        List<ExamComputer> list = new ArrayList<>();
        String sql = "select * from ExamComputer where status = 'Available' and areaId = ? order by computerCode";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamComputer ec = new ExamComputer();
                    ec.setId(rs.getInt("id"));
                    ec.setComputerCode(rs.getString("computerCode"));
                    ec.setAreaId(rs.getInt("areaId"));
                    ec.setStatus(rs.getString("status"));
                    ec.setLastUsedAt(rs.getTimestamp("lastUsedAt"));
                    list.add(ec);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "update ExamComputer set status = ?, lastUsedAt = getutcdate() where id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
