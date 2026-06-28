package dao;



public interface ExamDAO {
    int countAll();
    model.exam.Exam findById(int examId);
}

