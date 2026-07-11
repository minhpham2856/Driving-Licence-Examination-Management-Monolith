package examiner.service.impl;

import examiner.dao.ExaminerScheduleDAO;
import examiner.dao.impl.ExaminerScheduleDAOImpl;
import examiner.model.ExaminerSchedule;
import examiner.service.ScheduleService;

import java.util.List;

public class ScheduleServiceImpl implements ScheduleService {

    private final ExaminerScheduleDAO dao = new ExaminerScheduleDAOImpl();

    @Override
    public List<ExaminerSchedule> getSchedulesByExaminerId(int examinerUserId) {
        return dao.findByExaminerId(examinerUserId);
    }

    @Override
    public ExaminerSchedule getScheduleById(int examinerScheduleId) {
        return dao.getById(examinerScheduleId);
    }

    @Override
    public ExaminerSchedule getScheduleByExaminerAndExam(int examinerId, int examId) {
        return dao.getScheduleByExaminerAndExam(examinerId, examId);
    }
}
