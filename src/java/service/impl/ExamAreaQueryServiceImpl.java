package service.impl;

import dao.ExamAreaDAO;
import dao.ExaminerAssignmentDAO;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExaminerAssignmentDAOImpl;
import model.ExamArea;
import service.ExamAreaQueryService;
import util.examstaff.ExaminerAssignmentRules;

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
    public List<ExamArea> listActiveTheoryRooms() {
        return examAreaDAO.getActiveTheoryRooms();
    }

    @Override
    public List<ExamArea> listStaffedTheoryRoomsForExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamArea> examRooms = examAreaDAO.getAreasBySessionId(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedTheoryAreaIds(
                assignmentDAO.getBySessionId(examId));
        return ExaminerAssignmentRules.filterTheoryRoomsWithStaff(examRooms, staffed);
    }

    @Override
    public List<ExamArea> listStaffedPracticalAreasForExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamArea> examRooms = examAreaDAO.getAreasBySessionId(examId);
        Set<Integer> staffed = ExaminerAssignmentRules.staffedPracticalAreaIds(
                assignmentDAO.getBySessionId(examId));
        return ExaminerAssignmentRules.filterPracticalRoomsWithStaff(examRooms, staffed);
    }

    @Override
    public ExamArea findById(int examAreaId) {
        return examAreaDAO.getById(examAreaId);
    }
}
