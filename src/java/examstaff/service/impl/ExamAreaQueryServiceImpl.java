package examstaff.service.impl;

import examstaff.dao.ExamAreaDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamAreaDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import shared.model.ExamArea;
import examstaff.service.ExamAreaQueryService;
import examstaff.util.ExaminerAssignmentRules;

import java.util.List;
import java.util.Set;

/** Implementation: truy vấn khu vực thi đã có giám khảo phân công. */
public class ExamAreaQueryServiceImpl implements ExamAreaQueryService {

    private final ExamAreaDAO examAreaDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    /** Wiring mặc định khi không inject từ composition root. */
    public ExamAreaQueryServiceImpl() {
        this(new ExamAreaDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    /** Inject dependencies cho unit test / composition root. */
    public ExamAreaQueryServiceImpl(ExamAreaDAO examAreaDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.examAreaDAO = examAreaDAO;
        this.assignmentDAO = assignmentDAO;
    }

    /**
     * Phòng LT gắn kỳ và đã có sát hạch viên - dùng dropdown phân phòng thí sinh. 
     */
    @Override
    public List<ExamArea> listStaffedTheoryRoomsForExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamArea> examRooms = examAreaDAO.getAreasByExamId(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedTheoryAreaIds(
                assignmentDAO.getByExamId(examId));
        return ExaminerAssignmentRules.filterTheoryRoomsWithStaff(examRooms, staffed);
    }

    /**
     * Sân/phòng TH gắn kỳ và đã có sát hạch viên. 
     */
    @Override
    public List<ExamArea> listStaffedPracticalAreasForExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamArea> examRooms = examAreaDAO.getAreasByExamId(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedPracticalAreaIds(
                assignmentDAO.getByExamId(examId));
        return ExaminerAssignmentRules.filterPracticalRoomsWithStaff(examRooms, staffed);
    }

    /**
     * Tìm khu vực thi theo mã.
     *
     * @param examAreaId mã khu vực
     * @return khu vực, hoặc null nếu không có
     */
    @Override
    public ExamArea findById(int examAreaId) {
        return examAreaDAO.getById(examAreaId);
    }
}
