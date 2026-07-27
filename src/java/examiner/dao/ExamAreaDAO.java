package examiner.dao;
import shared.model.ExamArea;
import java.util.List;

// DAO contract for ExamArea persistence; examiner module SQL boundary.
public interface ExamAreaDAO {

    // Searches exam areas by keyword and optional area type filter.
    List<ExamArea> getFiltered(String keyword, String areaType);

    // Loads one exam area row by primary key.
    ExamArea get(int examAreaId);

    // Inserts a new exam area and returns generated id.
    int add(ExamArea area);

    // Updates an existing exam area row.
    boolean update(ExamArea area);

    // Deletes an exam area row by primary key.
    boolean delete(int examAreaId);

    // Returns total count of exam area rows.
    int countAll();

    // Lists active theory exam rooms (AreaType = exam room).
    List<ExamArea> getActiveTheoryRooms();

    // Lists exam areas linked to one exam via Exam_ExamArea.
    List<ExamArea> getAreasByExamId(int examId);

    // Lists exam areas for one exam filtered by area type.
    List<ExamArea> getAreasByExamIdAndType(int examId, String areaType);

    // Checks whether an area is assigned to an exam via ExaminerSchedule.
    boolean isAreaInExam(int examId, int examAreaId);
}
