package examiner.dao;

import examiner.enums.ExamStatus;
import java.util.List;
import examiner.model.Exam;

public interface ExamDAO {
    int countAll();
    Exam getById(int examId);
    List<Exam> getByStatus(ExamStatus status);
    List<Exam> getExamsByExaminerId(int examinerId);
}
