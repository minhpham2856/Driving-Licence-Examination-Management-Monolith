package examstaff.dao;
import shared.model.Question;
import java.util.List;
public interface QuestionDAO {
    List<Question> findByIds(List<Integer> questionIds);
    List<Question> findAll();
}

