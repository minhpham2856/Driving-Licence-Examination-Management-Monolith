package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.RoleDAO;
import Models.Role;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleDAOImpl implements RoleDAO {

    private final DBContext ctx;

    public RoleDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public Role getById(int id) {
        String sql = "select RoleId, RoleName from [Role] where RoleId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToRole(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Role getByName(String roleName) {
        String sql = "select RoleId, RoleName from [Role] where RoleName = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToRole(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Role mapToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("RoleId"));
        role.setRoleName(rs.getString("RoleName"));
        return role;
    }
}
