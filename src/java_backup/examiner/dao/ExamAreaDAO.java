package examiner.dao;
import shared.model.ExamArea;
import java.util.List;
public interface ExamAreaDAO {
    List<ExamArea> search(String keyword, String areaType);
    ExamArea getById(int examAreaId);
    int insert(ExamArea area);
    boolean update(ExamArea area);
    boolean delete(int examAreaId);
    int countAll();
    List<ExamArea> getActiveTheoryRooms();
    List<ExamArea> getAreasByExamId(int examId);
    boolean isAreaInExam(int examId, int examAreaId);
}

