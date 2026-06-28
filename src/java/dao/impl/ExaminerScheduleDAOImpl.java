package dao.impl;

import dbconnection.DBContext;
import dao.ExaminerScheduleDAO;
import model.exam.ExaminerSchedule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExaminerScheduleDAOImpl extends DBContext implements ExaminerScheduleDAO {

    private static final String BASE_SELECT = "SELECT ExaminerScheduleId, SessionId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt FROM ExaminerSchedule";

    @Override
    public boolean insert(ExaminerSchedule schedule) {
        String sql = "INSERT INTO ExaminerSchedule (SessionId, ExaminerId, ExamSectionId, ExamAreaId, AssignedBy, AssignedAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            ps.setTimestamp(6, schedule.getAssignedAt());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setExaminerScheduleId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int examinerScheduleId) {
        String sql = "DELETE FROM ExaminerSchedule WHERE ExaminerScheduleId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examinerScheduleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExaminerSchedule> getBySessionId(int sessionId) {
        String sql = BASE_SELECT + " WHERE SessionId = ?";
        return querySchedules(sql, ps -> ps.setInt(1, sessionId));
    }

    @Override
    public List<ExaminerSchedule> getByExaminerId(int examinerId) {
        String sql = BASE_SELECT + " WHERE ExaminerId = ?";
        return querySchedules(sql, ps -> ps.setInt(1, examinerId));
    }

    @Override
    public List<ExaminerSchedule> getBySessionIds(List<Integer> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE SessionId IN (");
        for (int i = 0; i < sessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        return querySchedules(sql.toString(), ps -> {
            for (int i = 0; i < sessionIds.size(); i++) {
                ps.setInt(i + 1, sessionIds.get(i));
            }
        });
    }

    private interface PreparedStatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<ExaminerSchedule> querySchedules(String sql, PreparedStatementBinder binder) {
        List<ExaminerSchedule> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExaminerSchedule map(ResultSet rs) throws SQLException {
        ExaminerSchedule s = new ExaminerSchedule();
        s.setExaminerScheduleId(rs.getInt("ExaminerScheduleId"));
        s.setSessionId(rs.getInt("SessionId"));
        s.setExaminerId(rs.getInt("ExaminerId"));
        
        int sectionId = rs.getInt("ExamSectionId");
        s.setExamSectionId(rs.wasNull() ? null : sectionId);
        
        int areaId = rs.getInt("ExamAreaId");
        s.setExamAreaId(rs.wasNull() ? null : areaId);
        
        int assignedBy = rs.getInt("AssignedBy");
        s.setAssignedBy(rs.wasNull() ? null : assignedBy);
        
        s.setAssignedAt(rs.getTimestamp("AssignedAt"));
        return s;
    }
}
