package DAOs;

import Models.ExamArea;
import java.util.List;

public interface ExamAreaDAO {
    List<ExamArea> getActiveTheoryRooms();
    List<ExamArea> getAllActiveAreas();
    List<ExamArea> getAreasBySessionId(int sessionId);
    boolean isAreaInSession(int sessionId, int areaId);
    ExamArea getById(int id);
    ExamArea getAreaByComputerCode(String computerCode);
}
