package examiner.dao;

import shared.enums.ExamStatus;
import java.util.List;
import shared.model.Exam;

// DAO contract for Exam persistence; examiner module SQL boundary.
public interface ExamDAO {

    // Returns total count of exam rows.
    int countAll();

    // Loads one exam row by primary key.
    Exam get(int examId);

    // Lists exams filtered by status.
    List<Exam> getByStatus(ExamStatus status);

    // Lists exams assigned to one examiner via ExaminerSchedule.
    List<Exam> getExamsByExaminerId(int examinerId);
}
