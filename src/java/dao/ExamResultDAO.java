package dao;
import model.ExamResult;
import java.util.List;
public interface ExamResultDAO {
    ExamResult findById(int examResultId);
    int insert(ExamResult result);
    boolean update(ExamResult result);
    boolean delete(int examResultId);
    int countAll();
    ExamResult getByCandidateId(int candidateId);
    boolean updateTheoryCorrectCount(int candidateId, int correct, int passThreshold);
}
