package service.impl;

import dao.ExamDAO;
import dao.impl.ExamDAOImpl;
import model.Exam;
import service.ExamService;

public class ExamServiceImpl implements ExamService {

    private final ExamDAO dao = new ExamDAOImpl();

    @Override
    public Exam getById(int examId) {
        return dao.getById(examId);
    }
}
