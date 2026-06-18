package DAOs.Impl;

import DAOs.ExamCandidateVehicleDAO;
import DBConnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamCandidateVehicleDAOImpl extends DBContext implements ExamCandidateVehicleDAO {

    @Override
    public Integer findExamDeviceId(int candidateId, int sessionId) {
        String sql = """
                SELECT ExamDeviceId FROM Exam_Candidate
                WHERE CandidateId = ? AND SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("ExamDeviceId");
                    return rs.wasNull() ? null : id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean assignExamDevice(int candidateId, int sessionId, int examDeviceId) {
        if (candidateId <= 0 || sessionId <= 0 || examDeviceId <= 0) {
            return false;
        }
        String sql = """
                UPDATE Exam_Candidate
                SET ExamDeviceId = ?
                WHERE CandidateId = ? AND SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds) {
        if (sessionId <= 0 || candidateIdsInOrder == null || candidateIdsInOrder.isEmpty()
                || availableDeviceIds == null || availableDeviceIds.isEmpty()) {
            return;
        }
        int deviceCount = availableDeviceIds.size();
        for (int i = 0; i < candidateIdsInOrder.size(); i++) {
            int candidateId = candidateIdsInOrder.get(i);
            if (candidateId <= 0) {
                continue;
            }
            Integer existing = findExamDeviceId(candidateId, sessionId);
            if (existing != null) {
                continue;
            }
            assignExamDevice(candidateId, sessionId, availableDeviceIds.get(i % deviceCount));
        }
    }

    @Override
    public Map<Integer, Map<String, Object>> findAssignmentDetailsBySession(int sessionId) {
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        if (sessionId <= 0) {
            return map;
        }
        String sql = """
                SELECT ec.CandidateId,
                       ec.ExamDeviceId,
                       ed.DeviceName,
                       ed.DeviceType,
                       ed.[Status]
                FROM Exam_Candidate ec
                LEFT JOIN ExamDevice ed ON ed.ExamDeviceId = ec.ExamDeviceId
                WHERE ec.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int deviceId = rs.getInt("ExamDeviceId");
                    row.put("deviceId", rs.wasNull() ? null : deviceId);
                    row.put("name", rs.getString("DeviceName"));
                    row.put("type", rs.getString("DeviceType"));
                    row.put("status", rs.getString("Status"));
                    map.put(rs.getInt("CandidateId"), row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
