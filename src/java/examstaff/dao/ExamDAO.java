package examstaff.dao;

import examstaff.enums.ExamStatus;
import java.util.List;
import examstaff.model.Exam;

public interface ExamDAO {
    int countAll();
    Exam getById(int examId);
    List<Exam> getByStatus(ExamStatus status);
    List<Exam> getExamsByExaminerId(int examinerId);
}
