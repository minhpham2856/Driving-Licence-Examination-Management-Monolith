package examstaff.dao;
import examstaff.model.Question;
import java.util.List;
public interface QuestionDAO {
    List<Question> findByIds(List<Integer> questionIds);
    List<Question> findAll();
}
