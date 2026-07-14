package general.dao.impl;
import general.dto.LicenceSearchCriteriaDTO;
import java.sql.*;
import general.dao.LicenceDAO;
import shared.dbconnection.DBContext;
import shared.model.Licence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
public class LicenceDAOImpl implements LicenceDAO {
    private static final String BASE_SELECT =
        "SELECT l.LicenceId, l.LicenceClass, l.Description, l.MinimumAge, l.ValidForYears, l.UpgradeFromLicenceId "
      + "FROM Licence l ";
    private static final String VEHICLE_TYPE_CASE =
        "CASE WHEN l.LicenceClass IN ('A1','A') THEN 'moto-2' "
      + "WHEN l.LicenceClass IN ('B1') THEN 'moto-3' "
      + "ELSE 'other' END";
    private static final String DURATION_CASE =
        "CASE WHEN l.LicenceClass IN ('A1','A') THEN 'duoi-3-thang' "
      + "WHEN l.LicenceClass IN ('B1') THEN 'tu-3-6-thang' "
      + "ELSE 'other' END";
    private Licence map(ResultSet rs) throws SQLException {
        // map db to model
        Licence l = new Licence();
        l.setLicenceId(rs.getInt("LicenceId"));
        l.setLicenceClass(rs.getString("LicenceClass"));
        l.setDescription(rs.getString("Description"));
        l.setMinimumAge(rs.getInt("MinimumAge"));
        l.setValidForYears(rs.getInt("ValidForYears"));
        
        // set nullable upgrade id
        int up = rs.getInt("UpgradeFromLicenceId");
        l.setUpgradeFromLicenceId(rs.wasNull() ? null : up);
        return l;
    }
    @Override
    public List<Licence> getAll() {
        // return all via search
        return search(null);
    }
    @Override
    public List<Licence> search(String keyword) {
        List<Licence> list = new ArrayList<>();
        
        // check keyword
        boolean hasKw = keyword != null && !keyword.isBlank();
        
        // prepare sql
        String sql = BASE_SELECT + (hasKw ? "WHERE l.LicenceClass LIKE ? OR l.Description LIKE ? " : "")
                   + "ORDER BY l.LicenceId";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
             
            // set parameters
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            
            // execute and map
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public List<Licence> searchByCriteria(LicenceSearchCriteriaDTO criteria) {
        List<Licence> list = new ArrayList<>();
        if (criteria == null) {
            return search(null);
        }

        String keyword = criteria.getKeyword();
        boolean hasKw = keyword != null && !keyword.isBlank();
        List<String> types = criteria.getVehicleTypes();
        List<String> durations = criteria.getDurations();
        boolean hasTypes = types != null && !types.isEmpty();
        boolean hasDurations = durations != null && !durations.isEmpty();

        String orderColumn = resolveSortColumn(criteria.getSortBy());
        String orderDir = "desc".equalsIgnoreCase(criteria.getSortDir()) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1 ");
        if (hasKw) {
            sql.append("AND (l.LicenceClass LIKE ? OR l.Description LIKE ?) ");
        }
        if (hasTypes) {
            sql.append("AND (").append(VEHICLE_TYPE_CASE).append(") IN (");
            for (int i = 0; i < types.size(); i++) {
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("?");
            }
            sql.append(") ");
        }
        if (hasDurations) {
            sql.append("AND (").append(DURATION_CASE).append(") IN (");
            for (int i = 0; i < durations.size(); i++) {
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("?");
            }
            sql.append(") ");
        }
        sql.append("ORDER BY ").append(orderColumn).append(" ").append(orderDir);

        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (hasTypes) {
                for (String type : types) {
                    ps.setString(idx++, type);
                }
            }
            if (hasDurations) {
                for (String duration : durations) {
                    ps.setString(idx++, duration);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) {
            return "l.LicenceId";
        }
        if ("licenceClass".equals(sortBy)) {
            return "l.LicenceClass";
        }
        if ("minimumAge".equals(sortBy)) {
            return "l.MinimumAge";
        }
        if ("validForYears".equals(sortBy)) {
            return "l.ValidForYears";
        }
        return "l.LicenceId";
    }
    @Override
    public Licence getById(int licenceId) {
        // prepare sql
        String sql = BASE_SELECT + "WHERE l.LicenceId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
             
            // set parameter
            ps.setInt(1, licenceId);
            
            // execute and map
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public Licence getByLicenceClass(String licenceClass) {
        // prepare sql
        String sql = BASE_SELECT + "WHERE l.LicenceClass = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
             
            // set parameter
            ps.setString(1, licenceClass);
            
            // execute and map
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
    @Override
    public boolean existsByClass(String licenceClass, int excludeId) {
        // prepare sql
        String sql = "SELECT COUNT(*) FROM Licence WHERE LicenceClass = ? AND LicenceId <> ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
             
            // set parameters
            ps.setString(1, licenceClass);
            ps.setInt(2, excludeId);
            
            // execute and map
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
        // prepare sql
        String sql = "INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            // set parameters
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            
            // execute
            if (ps.executeUpdate() > 0) {
                // get keys
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
        // prepare sql
        String sql = "UPDATE Licence SET LicenceClass = ?, Description = ?, MinimumAge = ?, ValidForYears = ?, "
                   + "UpgradeFromLicenceId = ? WHERE LicenceId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
             
            // set parameters
            ps.setString(1, l.getLicenceClass());
            ps.setString(2, l.getDescription());
            ps.setInt(3, l.getMinimumAge());
            ps.setInt(4, l.getValidForYears());
            setIntOrNull(ps, 5, l.getUpgradeFromLicenceId());
            ps.setInt(6, l.getLicenceId());
            
            // execute
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public int countAll() {
        // prepare sql
        String sql = "SELECT COUNT(*) FROM Licence";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // fetch count
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        // set int or null
        if (val == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, val);
    }
}

