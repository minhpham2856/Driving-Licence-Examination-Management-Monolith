package DAO.Impl;

import DBConnection.DBContext;
import DAO.RoleDAO;
import Models.Role;
import java.sql.*;

public class RoleDAOImpl extends DBContext implements RoleDAO {

    @Override
    public Role getById(int id) {
        String sql = """
                     select * from Role where id = ?
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Role(rs.getInt("id"), rs.getString("roleName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Role getByName(String roleName) {
        String sql = """
                     select * from Role where roleName = ?
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Role(rs.getInt("id"), rs.getString("roleName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
