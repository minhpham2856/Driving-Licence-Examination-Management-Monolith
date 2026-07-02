package dao;
import model.TheoryPaper;
import java.util.List;
public interface TheoryPaperDAO {
    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);
    List<TheoryPaper> getAllByExamEnrollmentIds(List<Integer> examEnrollmentIds);
}
