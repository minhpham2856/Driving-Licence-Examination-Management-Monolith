package examiner.service.impl;

import examiner.dao.ExamDAO;
import examiner.dao.impl.ExamDAOImpl;
import shared.model.Exam;
import examiner.service.ExamService;

public class ExamServiceImpl implements ExamService {

    private final ExamDAO dao = new ExamDAOImpl();

    @Override
    public Exam getById(int examId) {
        return dao.getById(examId);
    }
}

