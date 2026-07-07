package service.impl;

import dao.ExaminerScheduleDAO;
import dao.impl.ExaminerScheduleDAOImpl;
import model.ExaminerSchedule;
import service.ExaminerService;

import java.util.List;

public class ExaminerServiceImpl implements ExaminerService {

    private final ExaminerScheduleDAO dao = new ExaminerScheduleDAOImpl();

    @Override
    public List<ExaminerSchedule> getSchedulesByExaminerId(int examinerUserId) {
        return dao.findByExaminerId(examinerUserId);
    }

    @Override
    public ExaminerSchedule getScheduleById(int examinerScheduleId) {
        return dao.getById(examinerScheduleId);
    }
}
