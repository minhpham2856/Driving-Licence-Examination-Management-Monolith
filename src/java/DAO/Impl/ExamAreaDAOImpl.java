package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamAreaDAO;
import Models.ExamArea;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamAreaDAOImpl extends DBContext implements ExamAreaDAO {

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        List<ExamArea> list = new ArrayList<>();
        String sql = "select * from ExamArea where areaType = 'Room' and isActive = 1 order by id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ExamArea ea = new ExamArea();
                ea.setId(rs.getInt("id"));
                ea.setAreaName(rs.getString("areaName"));
                ea.setAreaType(rs.getString("areaType"));
                ea.setCapacity(rs.getInt("capacity"));
                ea.setLocation(rs.getString("location"));
                ea.setActive(rs.getBoolean("isActive"));
                list.add(ea);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ExamArea getById(int id) {
        String sql = "select * from ExamArea where id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExamArea getAreaByComputerCode(String computerCode) {
        String sql = "select ea.* from ExamArea ea join ExamComputer ec on ea.id = ec.areaId where ec.computerCode = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, computerCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
