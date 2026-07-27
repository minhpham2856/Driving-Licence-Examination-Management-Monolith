package examstaff.service.impl.support.allocation;
import examstaff.service.impl.support.assign.ExaminerAssignmentRules;

import examstaff.dao.ExamAreaDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamAreaDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.util.ExamAreaTypeResolver;
import shared.model.ExamArea;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Truy vấn khu vực thi đã có sát hạch viên phân công — phục vụ dropdown phân phòng thí sinh.
 * <p>
 * Gọi ExamAreaDAO lấy phòng gắn kỳ; ExaminerAssignmentDAO lấy slot staffed;
 * lọc qua ExaminerAssignmentRules. Được AllocationServiceImpl ủy quyền.
 *
 * API:
 * - listStaffedTheoryRoomsForExam — phòng LT gắn kỳ ∩ tập areaId đã có SHV
 * - listStaffedPracticalAreasForExam — sân TH tương tự
 * - findById — tra cứu một shared.model.ExamArea
 *
 * Quan hệ với assign:
 * Chỉ trả khu vực đã staffed — cùng quy tắc ExaminerAllocationServiceImpl dùng
 * trước auto-allocate; staff phải vào “Phân bổ sát hạch viên” trước khi phân phòng thí sinh.
 */
public class ExamAreaQueryServiceImpl {

    private final ExamAreaDAO examAreaDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    /**
     * Wiring mặc định khi không inject từ composition root.
     */
    public ExamAreaQueryServiceImpl() {
        this(new ExamAreaDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    /**
     * Inject dependencies cho unit test / composition root.
     * @param examAreaDAO    DAO khu vực thi
     * @param assignmentDAO  DAO phân công sát hạch viên
     */
    public ExamAreaQueryServiceImpl(ExamAreaDAO examAreaDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.examAreaDAO = examAreaDAO;
        this.assignmentDAO = assignmentDAO;
    }

    /**
     * Phòng LT gắn kỳ và đã có sát hạch viên — dùng dropdown phân phòng thí sinh.
     * @param examId mã kỳ thi
     * @return danh sách phòng LT đủ điều kiện (rỗng nếu examId không hợp lệ)
     */
    public List<ExamArea> listStaffedTheoryRoomsForExam(int examId) {
        // validate
        if (examId <= 0) {
            return List.of();
        }
        // load phòng gắn kỳ + tập areaId đã có SHV
        List<ExamArea> examRooms = listExamAreasWithFallback(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedTheoryAreaIds(
                assignmentDAO.getByExamId(examId));
        // result: chỉ phòng LT trong tập staffed
        return ExaminerAssignmentRules.filterTheoryRoomsWithStaff(examRooms, staffed);
    }

    /**
     * Sân/phòng TH gắn kỳ và đã có sát hạch viên.
     * @param examId mã kỳ thi
     * @return danh sách sân TH đủ điều kiện (rỗng nếu examId không hợp lệ)
     */
    public List<ExamArea> listStaffedPracticalAreasForExam(int examId) {
        // validate
        if (examId <= 0) {
            return List.of();
        }
        // load phòng gắn kỳ + tập areaId đã có SHV
        List<ExamArea> examRooms = listExamAreasWithFallback(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedPracticalAreaIds(
                assignmentDAO.getByExamId(examId));
        // result: chỉ sân TH trong tập staffed
        return ExaminerAssignmentRules.filterPracticalRoomsWithStaff(examRooms, staffed);
    }

    /**
     * Tìm khu vực thi theo mã.
     * @param examAreaId mã khu vực
     * @return khu vực, hoặc null nếu không có
     */
    public ExamArea findById(int examAreaId) {
        return examAreaDAO.getById(examAreaId);
    }

    /**
     * Ưu tiên khu vực gắn qua Exam_ExamArea; nếu kỳ cũ chưa có liên kết thì
     * fallback về danh mục phòng/sân và vẫn lọc theo phân công sát hạch viên.
     */
    private List<ExamArea> listExamAreasWithFallback(int examId) {
        List<ExamArea> linked = examAreaDAO.getAreasByExamId(examId);
        if (linked != null && !linked.isEmpty()) {
            return linked;
        }
        Map<Integer, ExamArea> byId = new LinkedHashMap<>();
        for (String type : List.of(
                ExamAreaTypeResolver.theoryAreaTypeLabel(),
                ExamAreaTypeResolver.theoryAreaTypeAlias(),
                ExamAreaTypeResolver.practicalAreaTypeLabel(),
                ExamAreaTypeResolver.practicalAreaTypeAlias())) {
            for (ExamArea area : examAreaDAO.getAvailableAreasByType(type)) {
                byId.putIfAbsent(area.getExamAreaId(), area);
            }
        }
        return List.copyOf(byId.values());
    }
}
