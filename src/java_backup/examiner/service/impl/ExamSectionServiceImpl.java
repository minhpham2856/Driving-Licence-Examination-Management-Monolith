package examiner.service.impl;

import examiner.dao.ExamSectionDAO;
import examiner.dao.impl.ExamSectionDAOImpl;
import shared.model.ExamSection;
import examiner.service.ExamSectionService;

public class ExamSectionServiceImpl implements ExamSectionService {
    private final ExamSectionDAO dao = new ExamSectionDAOImpl();

    @Override
    public ExamSection getById(int examSectionId) {
        return dao.getById(examSectionId);
    }
}

