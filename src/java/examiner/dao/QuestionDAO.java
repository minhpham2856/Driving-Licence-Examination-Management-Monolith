package examiner.dao;
import shared.model.Question;
import java.util.List;

// DAO contract for Question persistence; examiner module SQL boundary.
public interface QuestionDAO {

    // Loads question rows for a list of question ids.
    List<Question> getAllByIds(List<Integer> questionIds);

    // Loads all question rows.
    List<Question> getAll();
}
