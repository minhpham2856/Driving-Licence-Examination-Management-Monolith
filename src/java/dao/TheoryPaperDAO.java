package dao;
import model.TheoryPaper;
import dto.score.TheoryPaperAnswerDTO;
import java.util.List;
public interface TheoryPaperDAO {
    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);
    List<TheoryPaper> getAllByExamEnrollmentIds(List<Integer> examEnrollmentIds);
    List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd);
    int countQuestionsBySessionAndSbd(int sessionId, String sbd);
}
