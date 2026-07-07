package dao.impl;
import dao.SessionDAO;
import dbconnection.DBContext;
import enums.ExamSessionStatus;
import model.Session;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class SessionDAOImpl extends DBContext implements SessionDAO {
    @Override
    public Session getById(int id) {
        String sql = "SELECT * FROM [Session] WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<Session> findActive() {
        return findByWhere(
                " WHERE s.[Status] IN (?, ?, ?) ORDER BY s.StartTime",
                ExamSessionStatus.NOT_STARTED.getValue(),
                "Mở",
                ExamSessionStatus.IN_PROGRESS.getValue());
    }
    @Override
    public List<Session> findAllOrdered() {
        return findByWhere(" ORDER BY s.StartTime DESC");
    }
    @Override
    public List<Session> findByExamDate(Date examDate) {
        return findByWhere(" WHERE CAST(s.StartTime AS DATE) = ? ORDER BY s.StartTime", examDate);
    }
    @Override
    public boolean updateStatus(int sessionId, String status) {
        String sql = "UPDATE [Session] SET [Status] = ? WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public List<Integer> getExamAreaIds(int sessionId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT ExamAreaId FROM Session_ExamArea WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("ExamAreaId"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public Integer getExamSectionId(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }
        String sql = "SELECT TOP 1 ExamSectionId FROM Session_ExamSection WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public int countEnrollments(int sessionId) {
        String sql = "SELECT COUNT(*) FROM ExamEnrollment WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private List<Session> findByWhere(String suffix, Object... params) {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.* FROM [Session] s" + suffix;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Date) {
                    ps.setDate(i + 1, (Date) param);
                } else {
                    ps.setString(i + 1, String.valueOf(param));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    private static Session mapSession(ResultSet rs) throws SQLException {
        Session session = new Session();
        session.setSessionId(rs.getInt("SessionId"));
        session.setMorningSession(rs.getBoolean("IsMorningSession"));
        session.setStartTime(rs.getTimestamp("StartTime"));
        session.setEndTime(rs.getTimestamp("EndTime"));
        session.setStatus(rs.getString("Status"));
        session.setExamId(rs.getInt("ExamId"));
        return session;
    }
}
