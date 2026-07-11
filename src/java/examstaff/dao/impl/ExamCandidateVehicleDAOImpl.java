package examstaff.dao.impl;


import examstaff.dao.ExamCandidateVehicleDAO;

import dbconnection.DBContext;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JDBC implementation of ExamCandidateVehicleDAO for managing candidate-to-vehicle
 * (exam device) assignments during practical/road test sessions.
 * Implements round-robin distribution logic for fair device allocation.
 */
public class ExamCandidateVehicleDAOImpl extends DBContext implements ExamCandidateVehicleDAO {

    /**
     * Retrieves the ExamDeviceId currently assigned to a candidate in a given session.
     *
     * @param candidateId the CandidateId
     * @param sessionId   the SessionId
     * @return the assigned ExamDeviceId, or null if not assigned
     */
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

    /**
     * Assigns a device (vehicle) to a candidate for a given session by updating Exam_Candidate.
     *
     * @param candidateId  the CandidateId
     * @param sessionId    the SessionId
     * @param examDeviceId the ExamDeviceId to assign
     * @return true if the update succeeded
     */
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

    /**
     * Convenience overload that delegates to the full sync method with forceRedistribute=false.
     *
     * @param sessionId            the session ID
     * @param candidateIdsInOrder  ordered list of candidate IDs in the queue
     * @param availableDeviceIds   list of available device IDs
     * @param activeCandidateId    the candidate currently being tested (preserved if possible)
     */
    @Override
    public void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds, Integer activeCandidateId) {
        // Gán mặc định không bắt buộc chia lại toàn bộ nếu danh sách xe ổn định
        syncRoundRobinAssignments(sessionId, candidateIdsInOrder, availableDeviceIds, activeCandidateId, false);
    }

    /**
     * Synchronises candidate-to-vehicle assignments using a round-robin algorithm.
     * When forceRedistribute is false, existing stable assignments are preserved and
     * only unassigned candidates receive new devices. When true, all candidates are
     * reassigned (except the active candidate whose device is preserved if still available).
     * <p>
     * Business logic (Vietnamese):
     * - Bước 1: Thu thập thông tin gán xe hiện tại cho các thí sinh trong hàng đợi
     * - Bước 2: Kiểm tra nếu cần chạy lại toàn bộ phân phối (xe mới/mất, thiết bị bảo trì)
     * - Bước 3: Thực hiện gán Round-Robin hoặc giữ nguyên gán cũ tuỳ trường hợp
     *
     * @param sessionId            the session ID
     * @param candidateIdsInOrder  ordered list of candidate IDs in the queue
     * @param availableDeviceIds   list of available device IDs
     * @param activeCandidateId    the candidate currently being tested (preserved if possible)
     * @param forceRedistribute    if true, reassign all candidates regardless of current state
     */
    @Override
    public void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds, Integer activeCandidateId, boolean forceRedistribute) {
        if (sessionId <= 0 || candidateIdsInOrder == null || candidateIdsInOrder.isEmpty()
                || availableDeviceIds == null || availableDeviceIds.isEmpty()) {
            return;
        }

        // BƯỚC 1: Thu thập thông tin gán xe hiện tại của các thí sinh đang ở trong hàng đợi thi
        Map<Integer, Integer> currentAssignments = new HashMap<>();
        Set<Integer> assignedIds = new java.util.HashSet<>();
        for (int candidateId : candidateIdsInOrder) {
            if (candidateId <= 0) {
                continue;
            }
            // Lấy ID xe đã gán cho thí sinh
            Integer deviceId = findExamDeviceId(candidateId, sessionId);
            if (deviceId != null) {
                currentAssignments.put(candidateId, deviceId);
                assignedIds.add(deviceId);
            }
        }

        // BƯỚC 2: Kiểm tra xem có cần thiết phải chạy lại toàn bộ thuật toán phân phối xe hay không.
        boolean needFullRedistribution = forceRedistribute;
        
        if (!needFullRedistribution) {
            // Nếu chưa có thí sinh nào được gán xe -> assign
            if (assignedIds.isEmpty()) {
                needFullRedistribution = true;
            } else {
                // Case 1: Một xe đã được gán trước đó nhưng được đưa vào bảo trì
                for (Integer assignedId : assignedIds) {
                    if (!availableDeviceIds.contains(assignedId)) {
                        needFullRedistribution = true;
                        break;
                    }
                }
                // Case2: Một xe hoạt động trở lại
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

        // BƯỚC 3: Thực hiện gán xe dựa trên tình trạng phân bổ
        if (needFullRedistribution) {
            // Trường hợp A: PHÂN PHỐI LẠI TOÀN BỘ (Do thay đổi số lượng/trạng thái xe hoạt động)
            Integer activeAssignedDevice = activeCandidateId != null ? currentAssignments.get(activeCandidateId) : null;
            boolean activeDevicePreserved = false;
            if (activeAssignedDevice != null && availableDeviceIds.contains(activeAssignedDevice)) {
                activeDevicePreserved = true;
            }

            int deviceIndex = 0;
            for (int i = 0; i < candidateIdsInOrder.size(); i++) {
                int candidateId = candidateIdsInOrder.get(i);
                if (candidateId <= 0) {
                    continue;
                }

                // Nếu là thí sinh đang thi và xe cũ của họ vẫn còn sử dụng được, bỏ qua việc gán lại
                if (activeCandidateId != null && candidateId == activeCandidateId.intValue() && activeDevicePreserved) {
                    continue;
                }

                // Gán xe theo phương pháp Round-Robin (xoay vòng tuần tự theo thứ tự có sẵn trong danh sách xe khả dụng)
                int targetDevice = availableDeviceIds.get(deviceIndex % deviceCount);
                deviceIndex++;

                // Chỉ thực hiện cập nhật DB khi thông tin gán xe mới khác với xe hiện tại để giảm tải cho CSDL
                Integer current = currentAssignments.get(candidateId);
                if (current == null || !current.equals(targetDevice)) {
                    assignExamDevice(candidateId, sessionId, targetDevice);
                }
            }
        } else {
            // Trường hợp B: GIỮ NGUYÊN GÁN XE CŨ, CHỈ GÁN CHO THÍ SINH MỚI
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

    /**
     * Retrieves the full assignment details for all candidates in a session:
     * device ID, name, type, and status.
     *
     * @param sessionId the SessionId
     * @return a map keyed by CandidateId, each value containing device details
     */
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
