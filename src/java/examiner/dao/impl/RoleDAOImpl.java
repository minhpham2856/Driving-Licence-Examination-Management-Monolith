package examiner.dao.impl;
import examiner.dao.RoleDAO;
import shared.dbconnection.DBContext;
import shared.model.Role;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
// JDBC implementation for Role; examiner module DAO layer only.
public class RoleDAOImpl extends DBContext implements RoleDAO {
    private static final Logger LOG = Logger.getLogger(RoleDAOImpl.class.getName());
    // Loads one role row by primary key.
    @Override
    public Role get(int id) {
        String sql = "SELECT RoleId, RoleName FROM [Role] WHERE RoleId = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Role(rs.getInt("RoleId"), rs.getString("RoleName"));
                }
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Error getting role by id", ex);
        }
        return null;
    }
    // Loads one role row by role name string.
    @Override
    public Role getByName(String roleName) {
        String sql = "SELECT RoleId, RoleName FROM [Role] WHERE RoleName = ?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, roleName);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Role(rs.getInt("RoleId"), rs.getString("RoleName"));
                }
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Error getting role by name", ex);
        }
        return null;
    }
}

