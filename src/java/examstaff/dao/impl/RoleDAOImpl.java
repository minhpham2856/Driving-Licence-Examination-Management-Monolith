package examstaff.dao.impl;

import examstaff.dao.RoleDAO;
import shared.dbconnection.DBContext;
import shared.model.Role;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** JDBC implementation của {@link RoleDAO}. */
public class RoleDAOImpl extends DBContext implements RoleDAO {
    private static final Logger LOG = Logger.getLogger(RoleDAOImpl.class.getName());

    /** {@inheritDoc} */
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
