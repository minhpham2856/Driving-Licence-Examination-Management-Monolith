package examiner.dao.impl;
import java.sql.*;
import examiner.dao.LicenceDAO;
import shared.dbconnection.DBContext;
import shared.model.Licence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
// JDBC implementation for Licence; examiner module DAO layer only.
public class LicenceDAOImpl implements LicenceDAO {
    private static final String BASE_SELECT =
        "SELECT l.LicenceId, l.LicenceClass, l.Description, l.MinimumAge, l.ValidForYears, l.UpgradeFromLicenceId "
      + "FROM Licence l ";
    // Private helper: map.
    private Licence map(ResultSet rs) throws SQLException {
        Licence l = new Licence();
        l.setLicenceId(rs.getInt("LicenceId"));
        l.setLicenceClass(rs.getString("LicenceClass"));
        l.setDescription(rs.getString("Description"));
        l.setMinimumAge(rs.getInt("MinimumAge"));
        l.setValidForYears(rs.getInt("ValidForYears"));
        int up = rs.getInt("UpgradeFromLicenceId");
        l.setUpgradeFromLicenceId(rs.wasNull() ? null : up);
        return l;
    }
    // Lists all licence class rows.
    @Override
    public List<Licence> getAll() {
        return getFiltered(null);
    }
    // Searches licence rows by class code or description keyword.
    @Override
    public List<Licence> getFiltered(String keyword) {
        List<Licence> list = new ArrayList<>();
        boolean hasKw = keyword != null && !keyword.isBlank();
        String sql = BASE_SELECT + (hasKw ? "WHERE l.LicenceClass LIKE ? OR l.Description LIKE ? " : "")
                   + "ORDER BY l.LicenceId";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    // Loads one licence row by primary key.
    @Override
    public Licence get(int licenceId) {
        String sql = BASE_SELECT + "WHERE l.LicenceId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Loads one licence row by class code string.
    @Override
    public Licence getByLicenceClass(String licenceClass) {
        String sql = BASE_SELECT + "WHERE l.LicenceClass = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, licenceClass);
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
    // Checks whether another licence row already uses this class code.
    @Override
    public boolean existsByClass(String licenceClass, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Licence WHERE LicenceClass = ? AND LicenceId <> ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, licenceClass);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Inserts a new licence row and returns generated id.
    @Override
    public int add(Licence l) {
        String sql = "INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    // Updates an existing licence row.
    @Override
    public boolean update(Licence l) {
        String sql = "UPDATE Licence SET LicenceClass = ?, Description = ?, MinimumAge = ?, ValidForYears = ?, "
                   + "UpgradeFromLicenceId = ? WHERE LicenceId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            ps.setInt(6, l.getLicenceId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Returns total count of licence rows.
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Licence";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // Binds an integer parameter or SQL NULL when the value is absent.
    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, val);
    }
}

