package examstaff.dao;
import examstaff.model.ExamArea;
import java.util.List;
public interface ExamAreaDAO {
    ExamArea getById(int examAreaId);
    List<ExamArea> getActiveTheoryRooms();
    List<ExamArea> getAvailableAreasByType(String areaType);
    List<ExamArea> getAreasByExamId(int examId);
}
