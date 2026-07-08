package service;

import model.ExamArea;

import java.util.List;

public interface ExamAreaQueryService {

    List<ExamArea> listActiveTheoryRooms();

    ExamArea findById(int examAreaId);
}
