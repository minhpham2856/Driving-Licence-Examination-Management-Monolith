package DAOs;

import java.util.List;
import java.util.Map;

public interface ExamCandidateVehicleDAO {

    Integer findExamDeviceId(int candidateId, int sessionId);

    boolean assignExamDevice(int candidateId, int sessionId, int examDeviceId);

    /** Gán xe cho thí sinh chưa có xe - round-robin theo thứ tự hàng đợi. */
    void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds);

    Map<Integer, Map<String, Object>> findAssignmentDetailsBySession(int sessionId);
}
