package service.impl;

import dao.ExamSectionDAO;
import dao.impl.ExamSectionDAOImpl;
import model.ExamSection;
import service.ExamSectionService;

public class ExamSectionServiceImpl implements ExamSectionService {
    private final ExamSectionDAO dao = new ExamSectionDAOImpl();

    @Override
    public ExamSection getById(int examSectionId) {
        return dao.getById(examSectionId);
    }
}
