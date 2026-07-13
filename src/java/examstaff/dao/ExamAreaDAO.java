package examstaff.dao;

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

    // --- mainTest methods ---
    List<ExamArea> getAreasByExamId(int examId);

    boolean isAreaInExam(int examId, int examAreaId);

    // --- CleanMyBranch methods ---
    List<ExamArea> getAvailableAreasByType(String areaType);

    /** @deprecated dÃ¹ng {@link #getAreasByExamId(int)} */
    List<ExamArea> getAreasBySessionId(int sessionId);

    /** @deprecated dÃ¹ng {@link #isAreaInExam(int, int)} */
    boolean isAreaInSession(int sessionId, int examAreaId);
}

