package DAO;

import Models.ExamArea;
import java.util.List;

public interface ExamAreaDAO {
    List<ExamArea> getActiveTheoryRooms();
    ExamArea getById(int id);
    ExamArea getAreaByComputerCode(String computerCode);
}
