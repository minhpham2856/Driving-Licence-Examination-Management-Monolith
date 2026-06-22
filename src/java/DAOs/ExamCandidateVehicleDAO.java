package DAOs;

import java.util.List;
import java.util.Map;

// DAO cho thao tác phân bổ thiết bị (xe) cho thí sinh trong kỳ thi thực hành.
// Cung cấp các phương thức gán xe, đồng bộ phân bổ round-robin và tra cứu
// thông tin phân bổ chi tiết theo kỳ thi.
public interface ExamCandidateVehicleDAO {

    // Tìm mã thiết bị (xe) đã phân cho thí sinh trong kỳ thi.
    // 
    // @param candidateId mã thí sinh
    // @param sessionId   mã kỳ thi
    // @return Integer mã thiết bị, hoặc null nếu chưa được phân
    Integer findExamDeviceId(int candidateId, int sessionId);

    // Gán thiết bị (xe) cho thí sinh trong kỳ thi.
    // 
    // @param candidateId  mã thí sinh
    // @param sessionId    mã kỳ thi
    // @param examDeviceId mã thiết bị
    // @return true nếu gán thành công
    boolean assignExamDevice(int candidateId, int sessionId, int examDeviceId);

    // Đồng bộ phân bổ xe round-robin cho các thí sinh chưa có xe, theo thứ tự hàng đợi.
    // 
    // @param sessionId             mã kỳ thi
    // @param candidateIdsInOrder   danh sách mã thí sinh theo thứ tự ưu tiên
    // @param availableDeviceIds    danh sách mã thiết bị khả dụng
    // @param activeCandidateId     mã thí sinh đang hoạt động (có thể null)
    void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds, Integer activeCandidateId);

    // Đồng bộ phân bổ xe round-robin với tùy chọn phân phối lại.
    // 
    // @param sessionId             mã kỳ thi
    // @param candidateIdsInOrder   danh sách mã thí sinh theo thứ tự ưu tiên
    // @param availableDeviceIds    danh sách mã thiết bị khả dụng
    // @param activeCandidateId     mã thí sinh đang hoạt động (có thể null)
    // @param forceRedistribute     true nếu buộc phân phối lại từ đầu
    void syncRoundRobinAssignments(int sessionId, List<Integer> candidateIdsInOrder,
            List<Integer> availableDeviceIds, Integer activeCandidateId, boolean forceRedistribute);

    // Lấy thông tin chi tiết phân bổ thiết bị theo kỳ thi.
    // 
    // @param sessionId mã kỳ thi
    // @return Map với key là mã thí sinh, value là Map chứa thông tin phân bổ
    Map<Integer, Map<String, Object>> findAssignmentDetailsBySession(int sessionId);
}
