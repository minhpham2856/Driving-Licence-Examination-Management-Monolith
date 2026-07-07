package dao.impl;

import dao.ExaminerScheduleDAO;
import dbconnection.DBContext;
import enums.ExamSessionStatus;
import model.ExaminerSchedule;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExaminerScheduleDAOImpl extends DBContext implements ExaminerScheduleDAO {

    private static final String SCHEDULE_COLUMNS = """
            ExaminerScheduleId, SessionId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt
            """;

    @Override
    public boolean insert(ExaminerSchedule schedule) {
        String sql = """
                INSERT INTO ExaminerSchedule (SessionId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt)
                VALUES (?, ?, ?, ?, ?, GETDATE())
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, schedule.getSessionId());
            ps.setInt(2, schedule.getExaminerId());
            if (schedule.getExamSectionId() != null) {
                ps.setInt(3, schedule.getExamSectionId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (schedule.getExamAreaId() != null) {
                ps.setInt(4, schedule.getExamAreaId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            if (schedule.getAssignedBy() != null) {
                ps.setInt(5, schedule.getAssignedBy());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBySlot(int sessionId, int areaId, int examinerId) {
        String sql = """
                DELETE FROM ExaminerSchedule
                WHERE SessionId = ? AND ExamAreaId = ? AND ExaminerId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, areaId);
            ps.setInt(3, examinerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExaminerSchedule> getBySessionId(int sessionId) {
        String sql = "SELECT " + SCHEDULE_COLUMNS + " FROM ExaminerSchedule WHERE SessionId = ?";
        return queryList(sql, sessionId);
    }

    @Override
    public List<ExaminerSchedule> findByExamDate(Date examDate) {
        String sql = """
                SELECT es.ExaminerScheduleId, es.SessionId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Session] s ON s.SessionId = es.SessionId
                WHERE CAST(s.StartTime AS DATE) = ?
                ORDER BY s.StartTime, es.ExaminerScheduleId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, examDate);
            try (ResultSet rs = ps.executeQuery()) {
                List<ExaminerSchedule> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public Set<Integer> findBusyExaminerIdsByExamDate(Date examDate) {
        Set<Integer> busyIds = new HashSet<>();
        String sql = """
                SELECT DISTINCT es.ExaminerId
                FROM ExaminerSchedule es
                INNER JOIN [Session] s ON s.SessionId = es.SessionId
                WHERE CAST(s.StartTime AS DATE) = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, examDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    busyIds.add(rs.getInt("ExaminerId"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return busyIds;
    }

    @Override
    public List<ExaminerSchedule> findByExaminerId(int examinerUserId) {
        String sql = """
                SELECT es.ExaminerScheduleId, es.SessionId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Session] s ON s.SessionId = es.SessionId
                WHERE es.ExaminerId = ?
                ORDER BY s.StartTime DESC, es.ExaminerScheduleId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ExaminerSchedule> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public ExaminerSchedule getById(int examinerScheduleId) {
        String sql = "SELECT " + SCHEDULE_COLUMNS + " FROM ExaminerSchedule WHERE ExaminerScheduleId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerScheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSchedule(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExaminerSchedule> findInProgressByExaminerId(int examinerUserId) {
        String sql = """
                SELECT es.ExaminerScheduleId, es.SessionId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Session] s ON s.SessionId = es.SessionId
                WHERE es.ExaminerId = ? AND s.[Status] = ?
                ORDER BY s.StartTime, es.ExaminerScheduleId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerUserId);
            ps.setString(2, ExamSessionStatus.IN_PROGRESS.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                List<ExaminerSchedule> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<ExaminerSchedule> queryList(String sql, int param) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<ExaminerSchedule> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static ExaminerSchedule mapSchedule(ResultSet rs) throws SQLException {
        ExaminerSchedule schedule = new ExaminerSchedule();
        schedule.setExaminerScheduleId(rs.getInt("ExaminerScheduleId"));
        schedule.setSessionId(rs.getInt("SessionId"));
        schedule.setExaminerId(rs.getInt("ExaminerId"));
        int sectionId = rs.getInt("ExamSectionId");
        if (!rs.wasNull()) {
            schedule.setExamSectionId(sectionId);
        }
        int areaId = rs.getInt("ExamAreaId");
        if (!rs.wasNull()) {
            schedule.setExamAreaId(areaId);
        }
        int assignedBy = rs.getInt("AssignedBy");
        if (!rs.wasNull()) {
            schedule.setAssignedBy(assignedBy);
        }
        schedule.setAssignedAt(rs.getTimestamp("AssignedAt"));
        return schedule;
    }
}
