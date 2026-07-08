package service.impl;

import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import model.ExamArea;
import service.ExamAreaQueryService;

import java.util.List;

public class ExamAreaQueryServiceImpl implements ExamAreaQueryService {

    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();

    @Override
    public List<ExamArea> listActiveTheoryRooms() {
        return examAreaDAO.getActiveTheoryRooms();
    }

    @Override
    public ExamArea findById(int examAreaId) {
        return examAreaDAO.getById(examAreaId);
    }
}
