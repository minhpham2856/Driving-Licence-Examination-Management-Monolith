package dao.impl;


import dao.LicenceDAO;

import dbconnection.DBContext;

import model.licence.Licence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of LicenceDAO for managing driving licence types.
 * Each method opens its own connection via DBContext. Supports CRUD,
 * keyword search, and duplicate class checking.
 */
public class LicenceDAOImpl implements LicenceDAO {

    private static final String BASE_SELECT =
        "SELECT l.*, p.LicenceClass AS UpgradeFromClass "
      + "FROM Licence l LEFT JOIN Licence p ON l.UpgradeFromLicenceId = p.LicenceId ";

    /** Maps a ResultSet row to a Licence model including the upgrade-from class name. */
    private Licence map(ResultSet rs) throws SQLException {
        Licence l = new Licence();
        l.setLicenceId(rs.getInt("LicenceId"));
        l.setLicenceClass(rs.getString("LicenceClass"));
        l.setDescription(rs.getString("Description"));
        l.setMinimumAge(rs.getInt("MinimumAge"));
        l.setValidForYears(rs.getInt("ValidForYears"));
        int up = rs.getInt("UpgradeFromLicenceId");
        l.setUpgradeFromLicenceId(rs.wasNull() ? null : up);
        l.setUpgradeFromClass(rs.getString("UpgradeFromClass"));
        l.setCreatedAt(rs.getTimestamp("CreatedAt"));
        l.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        return l;
    }

    /**
     * Returns all licence records (delegates to search with null keyword).
     *
     * @return list of all Licence records
     */
    @Override
    public List<Licence> findAll() {
        return search(null);
    }

    /**
     * Searches licences by keyword matching LicenceClass or Description.
     *
     * @param keyword optional search text; if null/empty returns all records
     * @return list of matching Licence objects
     */
    @Override
    public List<Licence> search(String keyword) {
        List<Licence> list = new ArrayList<>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
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

    /**
     * Retrieves a licence by its primary key.
     *
     * @param licenceId the LicenceId
     * @return the Licence model, or null if not found
     */
    @Override
    public Licence findById(int licenceId) {
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

    /**
     * Checks whether a licence class already exists, excluding a specific ID
     * (used for uniqueness validation during update).
     *
     * @param licenceClass the class name to check
     * @param excludeId    the LicenceId to exclude from the check (0 for insert)
     * @return true if a record with the same class exists
     */
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

    /**
     * Inserts a new licence and returns the generated key.
     *
     * @param l the Licence to insert
     * @return the new LicenceId, or -1 on failure
     */
    @Override
    public int insert(Licence l) {
        String sql = "INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, "
                   + "UpgradeFromLicenceId, CreatedByUserId, UpdatedByUserId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            setIntOrNull(ps, 6, l.getCreatedByUserId());
            setIntOrNull(ps, 7, l.getUpdatedByUserId());
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

    /**
     * Updates all mutable fields of a licence, including the UpdatedAt timestamp.
     *
     * @param l the Licence with updated values
     * @return true if at least one row was updated
     */
    @Override
    public boolean update(Licence l) {
        String sql = "UPDATE Licence SET LicenceClass = ?, Description = ?, MinimumAge = ?, ValidForYears = ?, "
                   + "UpgradeFromLicenceId = ?, UpdatedAt = GETDATE(), UpdatedByUserId = ? WHERE LicenceId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            setIntOrNull(ps, 6, l.getUpdatedByUserId());
            ps.setInt(7, l.getLicenceId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the total number of licence records.
     *
     * @return the count
     */
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

    /** Helper to bind an Integer parameter as SQL NULL when the value is null. */
    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }
}
