package dao;
import model.Exam;
public interface ExamDAO {
    int countAll();
    Exam getById(int examId);
}
