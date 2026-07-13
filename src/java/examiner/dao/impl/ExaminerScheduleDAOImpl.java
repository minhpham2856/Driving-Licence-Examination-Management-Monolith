package examiner.dao.impl;

import examiner.dao.ExaminerScheduleDAO;
import shared.dbconnection.DBContext;
import shared.enums.ExamStatus;
import shared.model.ExaminerSchedule;
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
            ExaminerScheduleId, ExamId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt
            """;

    @Override
    public boolean insert(ExaminerSchedule schedule) {
        String sql = """
                INSERT INTO ExaminerSchedule (ExamId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt)
                VALUES (?, ?, ?, ?, ?, GETDATE())
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, schedule.getExamId());
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
    public boolean deleteBySlot(int examId, int areaId, int examinerId) {
        String sql = """
                DELETE FROM ExaminerSchedule
                WHERE ExamId = ? AND ExamAreaId = ? AND ExaminerId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, areaId);
            ps.setInt(3, examinerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExaminerSchedule> getByExamId(int examId) {
        String sql = "SELECT " + SCHEDULE_COLUMNS + " FROM ExaminerSchedule WHERE ExamId = ?";
        return queryList(sql, examId);
    }

    @Override
    public List<ExaminerSchedule> findByExamDate(Date examDate) {
        String sql = """
                SELECT es.ExaminerScheduleId, es.ExamId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Exam] e ON e.ExamId = es.ExamId
                WHERE CAST(e.StartTime AS DATE) = ?
                ORDER BY e.StartTime, es.ExaminerScheduleId
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
                INNER JOIN [Exam] e ON e.ExamId = es.ExamId
                WHERE CAST(e.StartTime AS DATE) = ?
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
                SELECT es.ExaminerScheduleId, es.ExamId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Exam] e ON e.ExamId = es.ExamId
                WHERE es.ExaminerId = ?
                ORDER BY e.StartTime DESC, es.ExaminerScheduleId DESC
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
    public ExaminerSchedule getScheduleByExaminerAndExam(int examinerId, int examId) {
        String sql = "SELECT " + SCHEDULE_COLUMNS
                + " FROM ExaminerSchedule WHERE ExaminerId = ? AND ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerId);
            ps.setInt(2, examId);
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
                SELECT es.ExaminerScheduleId, es.ExamId, es.ExaminerId, es.ExamSectionId,
                       es.ExamAreaId, es.AssignedBy, es.AssignedAt
                FROM ExaminerSchedule es
                INNER JOIN [Exam] e ON e.ExamId = es.ExamId
                WHERE es.ExaminerId = ? AND e.[Status] = ?
                ORDER BY e.StartTime, es.ExaminerScheduleId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerUserId);
            ps.setString(2, ExamStatus.IN_PROGRESS.getValue());
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
        schedule.setExamId(rs.getInt("ExamId"));
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

