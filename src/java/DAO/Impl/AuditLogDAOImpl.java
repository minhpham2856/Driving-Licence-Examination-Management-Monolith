package DAO.Impl;

import DBConnection.DBContext;
import DAO.AuditLogDAO;
import Models.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    @Override
    public boolean insert(AuditLog log) {
        String sql = """
                     insert into AuditLog (tableName, recordId, action, oldValue, newValue, changedBy, changedAt, ipAddress, sessionId)
                     values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 1. tableName: NOT NULL
            String tbl = log.getTableName();
            if (tbl == null || tbl.trim().isEmpty()) {
                tbl = "Person";
            }
            ps.setString(1, tbl);

            // 2. recordId: NOT NULL
            int recId = 0;
            if (log.getRecordId() != null) {
                recId = log.getRecordId();
            }
            ps.setInt(2, recId);

            // 3. action: NOT NULL & CHECK (action IN ('INSERT', 'UPDATE', 'DELETE', 'EXPORT'))
            String rawAct = log.getAction();
            String act = "UPDATE"; // Default
            if (rawAct != null) {
                String upper = rawAct.toUpperCase();
                if (upper.contains("INSERT")) act = "INSERT";
                else if (upper.contains("DELETE")) act = "DELETE";
                else if (upper.contains("EXPORT")) act = "EXPORT";
                else if (upper.contains("IMPORT")) act = "INSERT";
                else if (upper.contains("ALLOCATE")) act = "UPDATE";
                else if (upper.contains("ACTIVATE")) act = "UPDATE";
                else if (upper.contains("CALL")) act = "UPDATE";
                else if (upper.equals("INSERT") || upper.equals("UPDATE") || upper.equals("DELETE") || upper.equals("EXPORT")) {
                    act = upper;
                }
            }
            ps.setString(3, act);

            // 4. oldValue: Nullable
            if (log.getOldValue() != null) {
                ps.setString(4, log.getOldValue());
            } else {
                ps.setNull(4, Types.NVARCHAR);
            }

            // 5. newValue: Nullable
            if (log.getNewValue() != null) {
                ps.setString(5, log.getNewValue());
            } else {
                ps.setNull(5, Types.NVARCHAR);
            }

            // 6. changedBy: NOT NULL & REFERENCES [User](id)
            int userId = log.getChangedBy();
            if (userId <= 0) {
                userId = 3; // Default to staff1
            }
            ps.setInt(6, userId);

            // 7. changedAt: NOT NULL
            ps.setTimestamp(7, log.getChangedAt() != null ? log.getChangedAt() : new Timestamp(System.currentTimeMillis()));

            // 8. ipAddress: Nullable
            if (log.getIpAddress() != null) {
                ps.setString(8, log.getIpAddress());
            } else {
                ps.setNull(8, Types.NVARCHAR);
            }

            // 9. sessionId: Nullable
            if (log.getSessionId() != null) {
                ps.setString(9, log.getSessionId());
            } else {
                ps.setNull(9, Types.NVARCHAR);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("AuditLogDAOImpl insert failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<AuditLog> getLogsByUserToday(int userId) {
        List<AuditLog> list = new ArrayList<>();
        // Query logs created today by user, ordered by time descending
        String sql = """
                     select top 200 a.*, p.fullName as changerName 
                     from AuditLog a
                     left join [User] u on a.changedBy = u.id
                     left join Person p on u.personId = p.id
                     where a.changedBy = ? and a.changedAt >= CAST(GETDATE() AS DATE)
                     order by a.changedAt desc
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditLog> getAllLogsToday() {
        List<AuditLog> list = new ArrayList<>();
        String sql = """
                     select top 200 a.*, p.fullName as changerName 
                     from AuditLog a
                     left join [User] u on a.changedBy = u.id
                     left join Person p on u.personId = p.id
                     where a.changedAt >= CAST(GETDATE() AS DATE)
                     order by a.changedAt desc
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditLog> getLogsByUserAndDate(int userId, String dateStr) {
        List<AuditLog> list = new ArrayList<>();
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = """
                  select top 200 a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where a.changedBy = ? and CAST(a.changedAt AS DATE) = ?
                  order by a.changedAt desc
                  """;
        } else {
            sql = """
                  select top 200 a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where a.changedBy = ?
                  order by a.changedAt desc
                  """;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (hasDate) {
                ps.setString(2, dateStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditLog> getAllLogsByDate(String dateStr) {
        List<AuditLog> list = new ArrayList<>();
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = """
                  select top 200 a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where CAST(a.changedAt AS DATE) = ?
                  order by a.changedAt desc
                  """;
        } else {
            sql = """
                  select top 200 a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  order by a.changedAt desc
                  """;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (hasDate) {
                ps.setString(1, dateStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditLog> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
        List<AuditLog> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = """
                  select a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where a.changedBy = ? and CAST(a.changedAt AS DATE) = ?
                  order by a.changedAt desc
                  offset ? rows fetch next ? rows only
                  """;
        } else {
            sql = """
                  select a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where a.changedBy = ?
                  order by a.changedAt desc
                  offset ? rows fetch next ? rows only
                  """;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (hasDate) {
                ps.setString(2, dateStr);
                ps.setInt(3, offset);
                ps.setInt(4, pageSize);
            } else {
                ps.setInt(2, offset);
                ps.setInt(3, pageSize);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditLog> getAllLogsByDatePaginated(String dateStr, int page, int pageSize) {
        List<AuditLog> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = """
                  select a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  where CAST(a.changedAt AS DATE) = ?
                  order by a.changedAt desc
                  offset ? rows fetch next ? rows only
                  """;
        } else {
            sql = """
                  select a.*, p.fullName as changerName 
                  from AuditLog a
                  left join [User] u on a.changedBy = u.id
                  left join Person p on u.personId = p.id
                  order by a.changedAt desc
                  offset ? rows fetch next ? rows only
                  """;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (hasDate) {
                ps.setString(1, dateStr);
                ps.setInt(2, offset);
                ps.setInt(3, pageSize);
            } else {
                ps.setInt(1, offset);
                ps.setInt(2, pageSize);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int getLogsCountByUserAndDate(int userId, String dateStr) {
        int count = 0;
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = "select count(*) from AuditLog where changedBy = ? and CAST(changedAt AS DATE) = ?";
        } else {
            sql = "select count(*) from AuditLog where changedBy = ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (hasDate) {
                ps.setString(2, dateStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public int getAllLogsCountByDate(String dateStr) {
        int count = 0;
        String sql;
        boolean hasDate = dateStr != null && !dateStr.trim().isEmpty();
        
        if (hasDate) {
            sql = "select count(*) from AuditLog where CAST(changedAt AS DATE) = ?";
        } else {
            sql = "select count(*) from AuditLog";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (hasDate) {
                ps.setString(1, dateStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        log.setTableName(rs.getString("tableName"));
        log.setRecordId(rs.getInt("recordId"));
        if (rs.wasNull()) {
            log.setRecordId(null);
        }
        log.setAction(rs.getString("action"));
        log.setOldValue(rs.getString("oldValue"));
        log.setNewValue(rs.getString("newValue"));
        log.setChangedBy(rs.getInt("changedBy"));
        log.setChangedAt(rs.getTimestamp("changedAt"));
        log.setIpAddress(rs.getString("ipAddress"));
        log.setSessionId(rs.getString("sessionId"));
        log.setChangerName(rs.getString("changerName"));
        return log;
    }
}
