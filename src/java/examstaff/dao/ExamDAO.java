package examstaff.dao;

import shared.enums.ExamStatus;
import java.util.List;
import shared.model.Exam;

public interface ExamDAO {
    int countAll();
    Exam getById(int examId);
    List<Exam> getByStatus(ExamStatus status);
    List<Exam> getExamsByExaminerId(int examinerId);
}


