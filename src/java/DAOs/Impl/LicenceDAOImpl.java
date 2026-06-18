package DAO.Impl;

import DAO.LicenceDAO;
import DBConnection.DBContext;
import Models.Licence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LicenceDAOImpl implements LicenceDAO {

    private static final String BASE_SELECT =
        "SELECT l.*, p.LicenceClass AS UpgradeFromClass "
      + "FROM Licence l LEFT JOIN Licence p ON l.UpgradeFromLicenceId = p.LicenceId ";

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

    @Override
    public List<Licence> findAll() {
        return search(null);
    }

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

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }
}
