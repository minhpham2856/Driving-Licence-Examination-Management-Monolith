package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamCandidateVehicleDAO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExamCandidateVehicleDAOImpl implements ExamCandidateVehicleDAO {

    private final DBContext ctx;

    public ExamCandidateVehicleDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public Integer findExamDeviceId(int candidateId, int sessionId) {
        String sql = """
                select ExamDeviceId from ExamEnrollment
                where CandidateId = ? and SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
                update ExamEnrollment
                set ExamDeviceId = ?
                where CandidateId = ? and SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
            List<Integer> availableDeviceIds, Integer activeCandidateId) {
        syncRoundRobinAssignments(sessionId, candidateIdsInOrder, availableDeviceIds, activeCandidateId, false);
    }

    @Override
    public void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds, Integer activeCandidateId, boolean forceRedistribute) {
        if (sessionId <= 0 || candidateIdsInOrder == null || candidateIdsInOrder.isEmpty()
                || availableDeviceIds == null || availableDeviceIds.isEmpty()) {
            return;
        }

        Map<Integer, Integer> currentAssignments = new HashMap<>();
        Set<Integer> assignedIds = new java.util.HashSet<>();

        for (int candidateId : candidateIdsInOrder) {
            if (candidateId <= 0) {
                continue;
            }
            Integer deviceId = findExamDeviceId(candidateId, sessionId);
            if (deviceId != null) {
                currentAssignments.put(candidateId, deviceId);
                assignedIds.add(deviceId);
            }
        }

        boolean needFullRedistribution = forceRedistribute;

        if (!needFullRedistribution) {
            if (assignedIds.isEmpty()) {
                needFullRedistribution = true;
            } else {
                for (Integer assignedId : assignedIds) {
                    if (!availableDeviceIds.contains(assignedId)) {
                        needFullRedistribution = true;
                        break;
                    }
                }
                if (!needFullRedistribution && candidateIdsInOrder.size() >= availableDeviceIds.size()) {
                    for (Integer availableId : availableDeviceIds) {
                        if (!assignedIds.contains(availableId)) {
                            needFullRedistribution = true;
                            break;
                        }
                    }
                }
            }
        }

        int deviceCount = availableDeviceIds.size();

        if (needFullRedistribution) {
            Integer activeAssignedDevice = activeCandidateId != null ? currentAssignments.get(activeCandidateId) : null;
            boolean activeDevicePreserved = activeAssignedDevice != null && availableDeviceIds.contains(activeAssignedDevice);

            int deviceIndex = 0;
            for (int i = 0; i < candidateIdsInOrder.size(); i++) {
                int candidateId = candidateIdsInOrder.get(i);
                if (candidateId <= 0) {
                    continue;
                }
                if (activeCandidateId != null && candidateId == activeCandidateId.intValue() && activeDevicePreserved) {
                    continue;
                }
                int targetDevice = availableDeviceIds.get(deviceIndex % deviceCount);
                deviceIndex++;
                Integer current = currentAssignments.get(candidateId);
                if (current == null || !current.equals(targetDevice)) {
                    assignExamDevice(candidateId, sessionId, targetDevice);
                }
            }
        } else {
            for (int i = 0; i < candidateIdsInOrder.size(); i++) {
                int candidateId = candidateIdsInOrder.get(i);
                if (candidateId <= 0) {
                    continue;
                }
                Integer existing = currentAssignments.get(candidateId);
                if (existing == null) {
                    assignExamDevice(candidateId, sessionId, availableDeviceIds.get(i % deviceCount));
                }
            }
        }
    }

    @Override
    public Map<Integer, Map<String, Object>> findAssignmentDetailsBySession(int sessionId) {
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        if (sessionId <= 0) {
            return map;
        }

        String sql = """
                select ec.CandidateId,
                       ec.ExamDeviceId,
                       ed.DeviceName,
                       ed.DeviceType,
                       ed.[Status]
                from ExamEnrollment ec
                left join ExamDevice ed on ed.ExamDeviceId = ec.ExamDeviceId
                where ec.SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
