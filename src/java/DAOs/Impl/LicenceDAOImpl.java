package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.LicenceDAO;
import Models.Licence;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LicenceDAOImpl implements LicenceDAO {

    private final DBContext ctx;

    public LicenceDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<Licence> getAll() {
        return search(null);
    }

    @Override
    public List<Licence> search(String keyword) {
        List<Licence> list = new ArrayList<>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = (hasKw
                ? "select l.* from Licence l left join Licence p on l.UpgradeFromLicenceId = p.LicenceId where l.LicenceClass like ? or l.Description like ?"
                : "select l.* from Licence l left join Licence p on l.UpgradeFromLicenceId = p.LicenceId")
                + " order by l.LicenceId";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToLicence(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Licence getById(int licenceId) {
        String sql = "select l.* from Licence l left join Licence p on l.UpgradeFromLicenceId = p.LicenceId where l.LicenceId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, licenceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToLicence(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean existsByClass(String licenceClass, int excludeId) {
        String sql = "select count(*) from Licence where LicenceClass = ? and LicenceId <> ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, licenceClass);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int insert(Licence l) {
        String sql = """
                insert into Licence (LicenceClass, Description, MinimumAge, ValidForYears,
                    UpgradeFromLicenceId)
                values (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public boolean update(Licence l) {
        String sql = """
                update Licence
                set LicenceClass = ?, Description = ?, MinimumAge = ?, ValidForYears = ?,
                    UpgradeFromLicenceId = ?
                where LicenceId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            ps.setInt(6, l.getLicenceId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int countAll() {
        String sql = "select count(*) from Licence";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Licence mapToLicence(ResultSet rs) throws SQLException {
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

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) {
            ps.setNull(idx, java.sql.Types.INTEGER);
        } else {
            ps.setInt(idx, val);
        }
    }
}
