package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamAreaDAO;
import Models.ExamArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ExamAreaDAO for managing exam areas (rooms).
 * Uses a standalone DBContext connection per method.
 */
public class ExamAreaDAOImpl implements ExamAreaDAO {

    /** Maps a ResultSet row to an ExamArea model. */
    private ExamArea map(ResultSet rs) throws SQLException {
        ExamArea a = new ExamArea();
        a.setExamAreaId(rs.getInt("ExamAreaId"));
        a.setAreaName(rs.getString("AreaName"));
        a.setAreaType(rs.getString("AreaType"));
        a.setCapacity(rs.getInt("Capacity"));
        a.setLocation(rs.getString("Location"));
        if (hasColumn(rs, "CreatedAt")) {
            a.setCreatedAt(rs.getTimestamp("CreatedAt"));
        }
        if (hasColumn(rs, "UpdatedAt")) {
            a.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        }
        return a;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Searches exam areas by keyword (name/location/type) and optionally filters by area type.
     *
     * @param keyword  search text matching AreaName, Location, or AreaType
     * @param areaType optional exact match on AreaType
     * @return list of matching ExamArea objects
     */
    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ExamArea WHERE 1=1");
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = areaType != null && !areaType.trim().isEmpty();
        if (hasKw)   sql.append(" AND (AreaName LIKE ? OR Location LIKE ? OR AreaType LIKE ?)");
        if (hasType) sql.append(" AND AreaType = ?");
        sql.append(" ORDER BY ExamAreaId DESC");

        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasType) ps.setString(i++, areaType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Finds an exam area by its primary key (delegates to the same query as getById).
     *
     * @param examAreaId the ExamAreaId
     * @return the ExamArea, or null if not found
     */
    @Override
    public ExamArea findById(int examAreaId) {
        String sql = "SELECT * FROM ExamArea WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a new ExamArea and returns the generated key.
     *
     * @param a the ExamArea to insert
     * @return the new ExamAreaId, or -1 on failure
     */
    @Override
    public int insert(ExamArea a) {
        boolean hasAudit = hasTableColumn("ExamArea", "CreatedByUserId") && hasTableColumn("ExamArea", "UpdatedByUserId");
        String sql = hasAudit
                ? "INSERT INTO ExamArea (AreaName, AreaType, Capacity, Location, CreatedByUserId, UpdatedByUserId) "
                + "VALUES (?, ?, ?, ?, ?, ?)"
                : "INSERT INTO ExamArea (AreaName, AreaType, Capacity, Location) VALUES (?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());
            if (hasAudit) {
                setIntOrNull(ps, 5, a.getCreatedByUserId());
                setIntOrNull(ps, 6, a.getUpdatedByUserId());
            }
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
     * Updates all mutable fields of an exam area, including UpdatedAt timestamp.
     *
     * @param a the ExamArea with updated values
     * @return true if at least one row was updated
     */
    @Override
    public boolean update(ExamArea a) {
        boolean hasUpdatedAt = hasTableColumn("ExamArea", "UpdatedAt");
        boolean hasUpdatedBy = hasTableColumn("ExamArea", "UpdatedByUserId");
        String sql = "UPDATE ExamArea SET AreaName = ?, AreaType = ?, Capacity = ?, Location = ?"
                   + (hasUpdatedAt ? ", UpdatedAt = GETDATE()" : "")
                   + (hasUpdatedBy ? ", UpdatedByUserId = ?" : "")
                   + " WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());
            int idx = 5;
            if (hasUpdatedBy) {
                setIntOrNull(ps, idx++, a.getUpdatedByUserId());
            }
            ps.setInt(idx, a.getExamAreaId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes an exam area by ID. May fail with FK violation if still referenced.
     *
     * @param examAreaId the ExamAreaId to delete
     * @return true if deletion succeeded
     */
    @Override
    public boolean delete(int examAreaId) {
        String sql = "DELETE FROM ExamArea WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // most likely FK violation (area still referenced by devices/sessions)
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the total number of exam areas.
     *
     * @return the count
     */
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ExamArea";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Retrieves an exam area by ID (alias for findById).
     *
     * @param examAreaId the ExamAreaId
     * @return the ExamArea, or null
     */
    @Override
    public ExamArea getById(int examAreaId) {
        return findById(examAreaId);
    }

    /**
     * Returns all areas with AreaType = 'Lý thuyết' for theory exam assignment.
     *
     * @return list of theory exam areas
     */
    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamArea WHERE AreaType = N'Lý thuyết' ORDER BY AreaName";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns all exam areas assigned to a given session via Session_ExamArea.
     *
     * @param sessionId the SessionId
     * @return list of associated ExamArea objects
     */
    @Override
    public List<ExamArea> getAreasBySessionId(int sessionId) {
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT ea.* FROM ExamArea ea "
                   + "JOIN Session_ExamArea sea ON ea.ExamAreaId = sea.ExamAreaId "
                   + "WHERE sea.SessionId = ? ORDER BY ea.AreaName";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks whether a specific exam area is linked to a session.
     *
     * @param sessionId  the SessionId
     * @param examAreaId the ExamAreaId
     * @return true if the association exists
     */
    @Override
    public boolean isAreaInSession(int sessionId, int examAreaId) {
        String sql = "SELECT COUNT(*) FROM Session_ExamArea WHERE SessionId = ? AND ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Helper to bind an Integer parameter, setting SQL NULL when the value is null. */
    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }

    private boolean hasTableColumn(String tableName, String columnName) {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
