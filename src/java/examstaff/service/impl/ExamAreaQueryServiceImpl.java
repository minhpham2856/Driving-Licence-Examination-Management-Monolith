package examstaff.service.impl;

import examstaff.dao.ExamAreaDAO;
import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dao.impl.ExamAreaDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.model.ExamArea;
import examstaff.service.ExamAreaQueryService;
import examstaff.util.ExaminerAssignmentRules;

import java.util.List;
import java.util.Set;

public class ExamAreaQueryServiceImpl implements ExamAreaQueryService {

    private final ExamAreaDAO examAreaDAO;
    private final ExaminerAssignmentDAO assignmentDAO;

    public ExamAreaQueryServiceImpl() {
        this(new ExamAreaDAOImpl(), new ExaminerAssignmentDAOImpl());
    }

    public ExamAreaQueryServiceImpl(ExamAreaDAO examAreaDAO, ExaminerAssignmentDAO assignmentDAO) {
        this.examAreaDAO = examAreaDAO;
        this.assignmentDAO = assignmentDAO;
    }

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

    @Override
    public ExamArea findById(int examAreaId) {
        return examAreaDAO.getById(examAreaId);
    }
}
